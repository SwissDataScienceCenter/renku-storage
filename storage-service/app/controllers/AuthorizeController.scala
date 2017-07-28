package controllers

import javax.inject.{ Inject, Singleton }

import authorization.{ JWTVerifierProvider, ResourcesManagerJWTVerifierProvider }
import ch.datascience.graph.elements.mutation.{ GraphMutationClient, Mutation }
import ch.datascience.graph.elements.mutation.create.{ CreateEdgeOperation, CreateVertexOperation }
import ch.datascience.graph.elements.new_.NewEdge
import ch.datascience.graph.elements.new_.build.NewVertexBuilder
import ch.datascience.graph.elements.persisted.PersistedVertex
import ch.datascience.graph.naming.NamespaceAndName
import ch.datascience.graph.values.{ LongValue, StringValue }
import ch.datascience.service.security.ProfileFilterAction
import ch.datascience.service.utils.{ ControllerWithBodyParseJson, ControllerWithGraphTraversal }
import ch.datascience.service.models.resource.json._
import ch.datascience.service.models.storage.json._
import ch.datascience.graph.elements.mutation.log.model.json._
import ch.datascience.service.models.storage.{ CreateBucketRequest, CreateFileRequest, ReadResourceRequest, WriteResourceRequest }
import ch.datascience.service.utils.persistence.graph.{ GraphExecutionContextProvider, JanusGraphTraversalSourceProvider }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import ch.datascience.service.ResourceManagerClient
import controllers.storageBackends.Backends
import org.apache.tinkerpop.gremlin.structure.Vertex
import ch.datascience.service.utils.persistence.reader.VertexReader
import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import play.api.libs.json.{ JsObject, Json }

import scala.concurrent.Future

/**
 * Created by jeberle on 25.04.17.
 */
@Singleton
class AuthorizeController @Inject() (
    config:                                         play.api.Configuration,
    jwtVerifier:                                    JWTVerifierProvider,
    rmJwtVerifier:                                  ResourcesManagerJWTVerifierProvider,
    implicit val wsclient:                          WSClient,
    implicit val graphExecutionContextProvider:     GraphExecutionContextProvider,
    implicit val janusGraphTraversalSourceProvider: JanusGraphTraversalSourceProvider,
    implicit val vertexReader:                      VertexReader,
    backends:                                       Backends
) extends Controller with ControllerWithBodyParseJson with ControllerWithGraphTraversal with RequestHelper {

  lazy val mhost: String = config
    .getString( "graph.mutation.service.host" )
    .getOrElse( "http://graph-mutation:9000/api/mutation" )

  def get_property( persistedVertex: PersistedVertex, name: String ) =
    persistedVertex.properties.get( NamespaceAndName( name ) ).flatMap( v => v.values.headOption.map( value => value.asInstanceOf[StringValue].self ) )

  def get_creation_time( persistedVertex: PersistedVertex ) =
    persistedVertex.properties.get( NamespaceAndName( "system:creation_time" ) ).flatMap( v => v.values.headOption.map( value => value.asInstanceOf[LongValue].self ) )

  //TODO: factorize read and write !

  def objectRead = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[ReadResourceRequest]( ReadResourceRequestFormat ) ) { implicit request =>

    /* Steps:
     *   1. Resolve graph entities
     *   2. Request access authorization from Resource Manager
     *   3. Validate response from RM
     *   4. Log to Knowledge Graph
     *   5. Send authorization to client
     */

    // Step 1: Resolve graph entities
    val token: String = request.headers.get( "Authorization" ).getOrElse( "" )
    val rmc = new ResourceManagerClient( config )
    val g = graphTraversalSource
    val t = g.V( Long.box( request.body.resourceId ) ).union(
      __.has( "type", "resource:file_version" ),
      __.has( "type", "resource:file" ).in( "resource:version_of" ).order().by( "system:creation_time", Order.decr ).limit( 1 )
    ).as( "version" )
      .in( "resource:has_version" ).as( "data" )
      .out( "resource:stored_in" ).as( "bucket" )
      .select[Vertex]( "version", "data", "bucket" )

    graphExecutionContext.execute {
      if ( t.hasNext ) { //TODO: add some logic to select best alias
        import scala.collection.JavaConverters._
        val jmap: Map[String, Vertex] = t.next().asScala.toMap
        ( for {
          version <- jmap.get( "version" ).map( v => vertexReader.read( v ) )
          data <- jmap.get( "data" ).map( v => vertexReader.read( v ) )
          bucket <- jmap.get( "bucket" ).map( v => vertexReader.read( v ) )
        } yield {
          ( for { v <- version; d <- data; b <- bucket } yield {
            Some( Json.toJson( Map(
              "bucket" -> get_property( b, "resource:bucket_name" ).getOrElse( "" ),
              "name" -> ( get_property( d, "resource:path" ).getOrElse( "" ) + get_creation_time( v ).getOrElse( "" ) ),
              "backend" -> get_property( b, "resource:bucket_backend" ).getOrElse( "" )
            ) ).as[JsObject] )
          } ).flatMap( extra => {
            version.flatMap( v => {
              // Step 2: Request access authorization from Resource Manager
              rmc.authorize( AccessRequestFormat, request.body.toAccessRequest( extra ), token ).flatMap( ret => {
                // Step 3: Validate response from RM
                ret.map( ag => if ( ag.verifyAccessToken( rmJwtVerifier.get ).extraClaims.equals( extra ) ) {
                  request.executionId.map( eId => {
                    // Step 4: Log to KnowledgeGraph
                    val edge = NewEdge( NamespaceAndName( "resource:read" ), Right( eId ), Right( v.id ), Map() )
                    val mut = Mutation( Seq( CreateEdgeOperation( edge ) ) )
                    val gc = GraphMutationClient.makeStandaloneClient( mhost )
                    gc.postAndWait( mut ).map( ev => Ok( Json.toJson( ag ) ) )
                  } //TODO: maybe take into account if the node was created or not
                  // Step 5: Send authorization to client
                  ).getOrElse( Future( Ok( Json.toJson( ag ) ) ) )
                }
                else Future( InternalServerError( "Resource Manager response is invalid." ) ) ).getOrElse( Future( InternalServerError( "No response from Resource Manager." ) ) )
              } )
            } )
          } )
        } ).getOrElse( Future( NotFound ) )
      }
      else
        Future( NotFound )
    }
  }

  def objectWrite = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[WriteResourceRequest]( WriteResourceRequestFormat ) ) { implicit request =>
    /* Steps:
 *   1. Resolve graph entities
 *   2. Request access authorization from Resource Manager
 *   3. Validate response from RM
 *   4. Log to Knowledge Graph
 *   5. Send authorization to client
 */

    // Step 1: Resolve graph entities
    val token: String = request.headers.get( "Authorization" ).getOrElse( "" )
    val rmc = new ResourceManagerClient( config )
    val g = graphTraversalSource
    val t = g.V( Long.box( request.body.resourceId ) ).out( "resource:has_location" ).as( "data" ).out( "resource:stored_in" ).as( "bucket" ).select[Vertex]( "data", "bucket" )
    val now = System.currentTimeMillis
    graphExecutionContext.execute {
      if ( t.hasNext ) {
        import scala.collection.JavaConverters._
        val jmap: Map[String, Vertex] = t.next().asScala.toMap
        ( for {
          data <- jmap.get( "data" ).map( v => vertexReader.read( v ) )
          bucket <- jmap.get( "bucket" ).map( v => vertexReader.read( v ) )
        } yield {
          ( for { d <- data; b <- bucket } yield {
            Some( Json.toJson( Map(
              "bucket" -> get_property( b, "resource:bucket_name" ).getOrElse( "" ),
              "name" -> ( get_property( d, "resource:path" ).getOrElse( "" ) + now.toString ),
              "backend" -> get_property( b, "resource:bucket_backend" ).getOrElse( "" )
            ) ).as[JsObject] )
          } ).flatMap( extra => {
            data.flatMap( d => {
              // Step 2: Request access authorization from Resource Manager
              rmc.authorize( AccessRequestFormat, request.body.toAccessRequest( extra ), token ).flatMap( ret => {
                // Step 3: Validate response from RM
                ret.map( ag =>
                  if ( ag.verifyAccessToken( rmJwtVerifier.get ).extraClaims.equals( extra ) ) {
                  // Step 4: Log to KnowledgeGraph
                    val edges = (request.executionId.map( eId =>
                      Seq(NewEdge( NamespaceAndName( "resource:write" ), Right( eId ), Left( 1 ), Map() ))
                    ).getOrElse(Seq.empty) ++ Seq(
                      NewEdge( NamespaceAndName( "resource:has_version" ), Right( d.id ), Left( 1 ), Map() ),
                      NewEdge( NamespaceAndName( "resource:version_of" ), Left( 1 ), Right( request.body.resourceId ), Map() )
                    )).map(CreateEdgeOperation)

                      val version = new NewVertexBuilder( 1 )
                        .addSingleProperty( "system:creation_time", LongValue( now ) )
                        .addType( NamespaceAndName( "resource:file_version" ) ).result()
                      val mut = Mutation( Seq( CreateVertexOperation( version ) ) ++ edges )
                      val gc = GraphMutationClient.makeStandaloneClient( mhost )
                      gc.post( mut ).map( ev => Ok( Json.toJson( ag ) ) )
                    } //TODO: maybe take into account if the node was created or not
                    // Step 5: Send authorization to client
                else Future( InternalServerError( "Resource Manager response is invalid." ) ) ).getOrElse( Future( InternalServerError( "No response from Resource Manager." ) ) )
              } )
            } )
          } )
        } ).getOrElse( Future( NotFound ) )
      }
      else
        Future( NotFound )
    }
  }

  def objectCreate = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[CreateFileRequest]( CreateFileRequestFormat ) ) { implicit request =>
    /* Steps:
     *   1. Resolve graph entities
     *   2. Request access authorization from Resource Manager
     *   3. Validate response from RM and log to Knowledge Graph
     *   4. Send authorization to client
     */

    val token: String = request.headers.get( "Authorization" ).getOrElse( "" )
    val rmc = new ResourceManagerClient( config )
    val now = System.currentTimeMillis
    getVertex( request.body.bucketId ).flatMap {
      case Some( vertex ) =>
        if ( vertex.types.contains( NamespaceAndName( "resource:bucket" ) ) ) {

          val backend = get_property( vertex, "resource:bucket_backend" ).getOrElse( "" )
          val extra = Some( Json.toJson( Map(
            "bucket" -> get_property( vertex, "resource:bucket_name" ).getOrElse( "" ),
            "name" -> ( request.body.fileName + now.toString ),
            "backend" -> backend
          ) ).as[JsObject] )
          rmc.authorize( AccessRequestFormat, request.body.toAccessRequest( extra ), token ).flatMap( res => {
            res.map( ag => if ( ag.verifyAccessToken( rmJwtVerifier.get ).extraClaims.equals( extra ) ) {
              val fvertex = new NewVertexBuilder( 1 )
                .addSingleProperty( "resource:file_name", StringValue( request.body.fileName ) )
                .addType( NamespaceAndName( "resource:file" ) )
              val lvertex = new NewVertexBuilder( 2 )
                .addSingleProperty( "resource:path", StringValue( request.body.fileName ) )
                .addType( NamespaceAndName( "resource:file_location" ) )
              val vvertex = new NewVertexBuilder( 3 )
                .addSingleProperty( "system:creation_time", LongValue( now ) )
                .addType( NamespaceAndName( "resource:file_version" ) )
              val edges = Seq(
                NewEdge( NamespaceAndName( "resource:stored_in" ), Left( lvertex.tempId ), Right( vertex.id ), Map() ),
                NewEdge( NamespaceAndName( "resource:version_of" ), Left( vvertex.tempId ), Left( fvertex.tempId ), Map() ),
                NewEdge( NamespaceAndName( "resource:has_version" ), Left( lvertex.tempId ), Left( vvertex.tempId ), Map() ),
                NewEdge( NamespaceAndName( "resource:has_location" ), Left( fvertex.tempId ), Left( lvertex.tempId ), Map() )
              )
              val createAndWriteEdges = ( request.executionId.map { execId =>
                Seq(
                  NewEdge( NamespaceAndName( "resource:write" ), Right( execId ), Left( vvertex.tempId ), Map() ),
                  NewEdge( NamespaceAndName( "resource:create" ), Right( execId ), Left( vvertex.tempId ), Map() )
                )
              }.getOrElse( Seq.empty ) ++ edges ).map( CreateEdgeOperation )
              val vertices = Seq( fvertex, lvertex, vvertex ).map( _.result() ).map( CreateVertexOperation )
              val mut = Mutation( vertices ++ createAndWriteEdges )
              val gc = GraphMutationClient.makeStandaloneClient( mhost )
              gc.postAndWait( mut ).map( ev => Ok( Json.toJson( ag ) ) ) //TODO: maybe take into account if the node was created or not
            }
            else Future( InternalServerError( "Resource Manager response is invalid." ) ) ).getOrElse( Future( InternalServerError( "No response from Resource Manager." ) ) )
          } )
        }
        else {
          Future( BadRequest( "Resource is not a bucket" ) )
        }
      case None => Future( BadRequest( "Unknown resource Id" ) )
    }

  }

  def bucketCreate = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[CreateBucketRequest]( CreateBucketRequestFormat ) ) { implicit request =>
    /* Steps:
     *   1. Request access authorization from Resource Manager
     *   2. Validate response from RM
     *   3. Create bucket
     *   4. Log to Knowledge Graph
     *   5. Send response to client
     */

    val token: String = request.headers.get( "Authorization" ).getOrElse( "" )
    val rmc = new ResourceManagerClient( config )
    val backend = request.body.backend
    val name = request.body.name
    val extra = Some( Json.toJson( Map(
      "bucket" -> name,
      "backend" -> backend
    ) ).as[JsObject] )

    rmc.authorize( AccessRequestFormat, request.body.toAccessRequest( extra ), token ).flatMap( res =>
      res.map( ag => if ( ag.verifyAccessToken( rmJwtVerifier.get ).extraClaims.equals( extra ) ) {
        backends.getBackend( backend ) match {
          case Some( back ) =>
            val bid = back.createBucket( request, request.body.name )
            val vertex = new NewVertexBuilder()
              .addSingleProperty( "resource:bucket_backend_id", StringValue( bid ) )
              .addSingleProperty( "resource:bucket_name", StringValue( name ) )
              .addSingleProperty( "resource:bucket_backend", StringValue( backend ) )
              .addType( NamespaceAndName( "resource:bucket" ) )
            val mut = Mutation( Seq( CreateVertexOperation( vertex.result() ) ) )
            val gc = GraphMutationClient.makeStandaloneClient( mhost )
            gc.post( mut ).flatMap( ev => gc.wait( ev.uuid ).map( e => Created( Json.toJson( e ) ) ) )

          case None => Future( BadRequest( s"The backend $backend is not enabled." ) )
        }
      }
      else Future( InternalServerError( "Resource Manager response is invalid." ) ) ).getOrElse( Future( InternalServerError( "No response from Resource Manager." ) ) ) )
  }
}
