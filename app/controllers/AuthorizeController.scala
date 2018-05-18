/*
 * Copyright 2017 - Swiss Data Science Center (SDSC)
 * A partnership between École Polytechnique Fédérale de Lausanne (EPFL) and
 * Eidgenössische Technische Hochschule Zürich (ETHZ).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import javax.inject.{ Inject, Singleton }

import authorization.{ JWTVerifierProvider, ResourcesManagerJWTVerifierProvider }
import ch.datascience.graph.elements.mutation.{ GraphMutationClient, Mutation }
import ch.datascience.graph.elements.mutation.create.{ CreateEdgeOperation, CreateVertexOperation }
import ch.datascience.graph.elements.mutation.log.model.{ EventStatus, MutationFailed, MutationResponse, MutationSuccess }
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
import ch.datascience.service.models.storage.{ CopyFileRequest, CreateBucketRequest, CreateFileRequest, ReadResourceRequest, WriteResourceRequest }
import ch.datascience.service.utils.persistence.graph.{ GraphExecutionContextProvider, JanusGraphTraversalSourceProvider }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import ch.datascience.service.ResourceManagerClient
import ch.datascience.service.models.resource.AccessGrant
import controllers.storageBackends.Backends
import org.apache.tinkerpop.gremlin.structure.Vertex
import ch.datascience.service.utils.persistence.reader.VertexReader
import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.Future

/**
 * Created by jeberle on 25.04.17.
 */
@Singleton
class AuthorizeController @Inject() (
    config:                                         play.api.Configuration,
    jwtVerifier:                                    JWTVerifierProvider,
    rmJwtVerifier:                                  ResourcesManagerJWTVerifierProvider,
    graphMutationClientProvider:                    GraphMutationClientProvider,
    implicit val wsclient:                          WSClient,
    implicit val graphExecutionContextProvider:     GraphExecutionContextProvider,
    implicit val janusGraphTraversalSourceProvider: JanusGraphTraversalSourceProvider,
    implicit val vertexReader:                      VertexReader,
    backends:                                       Backends
) extends Controller with ControllerWithBodyParseJson with ControllerWithGraphTraversal with RequestHelper {

  lazy val gc: GraphMutationClient = graphMutationClientProvider.get

  lazy val logger: Logger = Logger( "application.AuthorizeController" )

  def get_property( persistedVertex: PersistedVertex, name: String ) =
    persistedVertex.properties.get( NamespaceAndName( name ) ).flatMap( v => v.values.headOption.map( value => value.asInstanceOf[StringValue].self ) )

  def get_creation_time( persistedVertex: PersistedVertex ) =
    persistedVertex.properties.get( NamespaceAndName( "system:creation_time" ) ).flatMap( v => v.values.headOption.map( value => value.asInstanceOf[LongValue].self ) )

  //TODO: factorize read and write !

  def objectRead = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[ReadResourceRequest]( ReadResourceRequestFormat ) ) { implicit request =>
    logger.info( s"objectRead - ${request.body} - ${request.token.getSubject}" )

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
              "bucket" -> get_property( b, "resource:bucket_backend_id" ).getOrElse( "" ),
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
                    gc.postAndWait( mut ).map( ev => Ok( Json.toJson( ag ) ) )
                  } //TODO: maybe take into account if the node was created or not
                  // Step 5: Send authorization to client
                  ).getOrElse( Future( Ok( Json.toJson( ag ) ) ) )
                }
                else {
                  logger.error( s"Resource Manager response is invalid. Got: $ag Expected extras: $extra" )
                  Future( InternalServerError( "Resource Manager response is invalid." ) )
                } ).getOrElse {
                  logger.error( s"No response from Resource Manager" )
                  Future( InternalServerError( "No response from Resource Manager." ) )
                }
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
    logger.info( s"objectWrite - ${request.body} - ${request.token.getSubject}" )

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
              "bucket" -> get_property( b, "resource:bucket_backend_id" ).getOrElse( "" ),
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
                    val edges = ( request.executionId.map( eId =>
                      Seq( NewEdge( NamespaceAndName( "resource:write" ), Right( eId ), Left( 1 ), Map() ) ) ).getOrElse( Seq.empty ) ++ Seq(
                      NewEdge( NamespaceAndName( "resource:has_version" ), Right( d.id ), Left( 1 ), Map() ),
                      NewEdge( NamespaceAndName( "resource:version_of" ), Left( 1 ), Right( request.body.resourceId ), Map() )
                    ) ).map( CreateEdgeOperation )

                    val version = new NewVertexBuilder( 1 )
                      .addSingleProperty( "system:creation_time", LongValue( now ) )
                      .addSingleProperty( "resource:owner", StringValue( request.userId ) )
                      .addType( NamespaceAndName( "resource:file_version" ) ).result()
                    val mut = Mutation( Seq( CreateVertexOperation( version ) ) ++ edges )
                    gc.postAndWait( mut ).map( ev => Ok( Json.toJson( ag ) ) )
                  } //TODO: maybe take into account if the node was created or not
                  // Step 5: Send authorization to client
                  else {
                    logger.error( s"Resource Manager response is invalid. Got: $ag Expected extras: $extra" )
                    Future( InternalServerError( "Resource Manager response is invalid." ) )
                  } ).getOrElse {
                  logger.error( s"No response from Resource Manager" )
                  Future( InternalServerError( "No response from Resource Manager." ) )
                }
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
    logger.info( s"objectCreate - ${request.body} - ${request.token.getSubject}" )

    /* Steps:
     *   1. Resolve graph entities
     *   2. Request access authorization from Resource Manager
     *   3. Validate response from RM and log to Knowledge Graph
     *   4. Send authorization to client
     */

    val token: String = request.headers.get( "Authorization" ).getOrElse( "" )
    val rmc = new ResourceManagerClient( config )
    val now = System.currentTimeMillis
    val projectId = request.headers.get( "Renga-Projects-Project" ).map( _.toLong )
    getVertex( request.body.bucketId ).flatMap {
      case Some( vertex ) =>
        if ( vertex.types.contains( NamespaceAndName( "resource:bucket" ) ) ) {

          val backend = get_property( vertex, "resource:bucket_backend" ).getOrElse( "" )
          val extra = Some( Json.toJson( Map(
            "bucket" -> get_property( vertex, "resource:bucket_backend_id" ).getOrElse( "" ),
            "name" -> ( request.body.fileName + now.toString ),
            "backend" -> backend
          ) ).as[JsObject] )
          rmc.authorize( AccessRequestFormat, request.body.toAccessRequest( extra ), token ).flatMap( res => {
            res.map( ag => if ( ag.verifyAccessToken( rmJwtVerifier.get ).extraClaims.equals( extra ) ) {
              val fvertex = new NewVertexBuilder( 1 )
                .addSingleProperty( "resource:file_name", StringValue( request.body.fileName ) )
                .addSingleProperty( "resource:owner", StringValue( request.userId ) )
                .addLabels( request.body.labels )
                .addType( NamespaceAndName( "resource:file" ) )
                .result()
              val lvertex = new NewVertexBuilder( 2 )
                .addSingleProperty( "resource:path", StringValue( request.body.fileName ) )
                .addSingleProperty( "resource:owner", StringValue( request.userId ) )
                .addType( NamespaceAndName( "resource:file_location" ) )
                .result()
              val vvertex = new NewVertexBuilder( 3 )
                .addSingleProperty( "system:creation_time", LongValue( now ) )
                .addSingleProperty( "resource:owner", StringValue( request.userId ) )
                .addType( NamespaceAndName( "resource:file_version" ) )
                .result()
              val edges = Seq(
                NewEdge( NamespaceAndName( "resource:stored_in" ), Left( lvertex.tempId ), Right( vertex.id ), Map() ),
                NewEdge( NamespaceAndName( "resource:version_of" ), Left( vvertex.tempId ), Left( fvertex.tempId ), Map() ),
                NewEdge( NamespaceAndName( "resource:has_version" ), Left( lvertex.tempId ), Left( vvertex.tempId ), Map() ),
                NewEdge( NamespaceAndName( "resource:has_location" ), Left( fvertex.tempId ), Left( lvertex.tempId ), Map() )
              )
              val createAndWriteEdges = request.executionId.map { execId =>
                Seq(
                  NewEdge( NamespaceAndName( "resource:write" ), Right( execId ), Left( vvertex.tempId ), Map() ),
                  NewEdge( NamespaceAndName( "resource:create" ), Right( execId ), Left( vvertex.tempId ), Map() )
                )
              }.getOrElse( Seq.empty )
              val projectEdge = projectId.map { pid =>
                NewEdge( NamespaceAndName( "project:is_part_of" ), Left( fvertex.tempId ), Right( pid ), Map() )
              }
              val vertices = Seq( fvertex, lvertex, vvertex ).map( CreateVertexOperation )
              val allEdges = ( edges ++ createAndWriteEdges ++ projectEdge.toSeq ).map( CreateEdgeOperation )
              val mut = Mutation( vertices ++ allEdges ) // First vertex is the file vertex (used later)
              //TODO: maybe take into account if the node was created or not
              gc.postAndWait( mut ).map { ev =>
                val response = ev.status match {
                  case EventStatus.Completed( res ) => res
                  case EventStatus.Pending          => throw new RuntimeException( s"Expected completed mutation: ${ev.uuid}" )
                }

                val mutationResponse = response.event.as[MutationResponse]
                val fileVertexId = mutationResponse match {
                  case MutationSuccess( results ) => results.head // First vertex is the file vertex
                  case MutationFailed( reason )   => throw new RuntimeException( s"Bucket creation failed, caused by: $reason" )
                }

                import play.api.libs.functional.syntax._
                implicit val writes: OWrites[( AccessGrant, JsObject )] = ( JsPath.write[AccessGrant] and JsPath.write[JsObject] )( unlift( Tuple2.unapply[AccessGrant, JsObject] ) )

                Created( Json.toJson( ( ag, fileVertexId ) ) )
              }
            }
            else {
              logger.error( s"Resource Manager response is invalid. Got: $ag Expected extras: $extra" )
              Future( InternalServerError( "Resource Manager response is invalid." ) )
            } ).getOrElse {
              logger.error( s"No response from Resource Manager" )
              Future( InternalServerError( "No response from Resource Manager." ) )
            }
          } )
        }
        else {
          logger.info( s"Resource ${request.body.bucketId} is not a bucket" )
          Future( BadRequest( "Resource is not a bucket" ) )
        }
      case None =>
        logger.info( s"Unknown resource Id ${request.body.bucketId}" )
        Future( BadRequest( "Unknown resource Id" ) )
    }

  }

  def objectDuplicate = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[CopyFileRequest]( CopyFileRequestFormat ) ) { implicit request =>
    logger.info( s"objectDuplicate - ${request.body} - ${request.token.getSubject}" )

    /* Steps:
     *   1. Resolve graph entities
     *   2. Request access authorization from Resource Manager
     *   3. Validate response from RM and log to Knowledge Graph
     *   4. Perform copy
     */

    val token: String = request.headers.get( "Authorization" ).getOrElse( "" )
    val rmc = new ResourceManagerClient( config )
    val now = System.currentTimeMillis
    val projectId = request.headers.get( "Renga-Projects-Project" ).map( _.toLong )

    val newBucket = request.body.bucketId.map( bid => getVertex( bid ) ).getOrElse( Future( None ) )

    val g = graphTraversalSource
    val t = g.V( Long.box( request.body.resourceId ) ).union(
      __.has( "type", "resource:file_version" ),
      __.has( "type", "resource:file" ).in( "resource:version_of" ).order().by( "system:creation_time", Order.decr ).limit( 1 )
    ).as( "version" )
      .in( "resource:has_version" ).as( "data" )
      .out( "resource:stored_in" ).as( "bucket" )
      .select[Vertex]( "version", "data", "bucket" )

    newBucket.flatMap { bucketVertex =>
      if ( bucketVertex.forall( b => b.types.contains( NamespaceAndName( "resource:bucket" ) ) ) ) {
        graphExecutionContext.execute {
          if ( t.hasNext ) { //TODO: add some logic to select best alias
            import scala.collection.JavaConverters._
            val jmap: Map[String, Vertex] = t.next().asScala.toMap
            ( for {
              version <- jmap.get( "version" ).map( v => vertexReader.read( v ) )
              data <- jmap.get( "data" ).map( v => vertexReader.read( v ) )
              bucket <- jmap.get( "bucket" ).map( v => vertexReader.read( v ) )
            } yield {
              for {
                v <- version; d <- data; b <- bucket; res <- {
                  val dest_bucket = bucketVertex.getOrElse( b )
                  val backend = get_property( dest_bucket, "resource:bucket_backend" ).getOrElse( "" )
                  if ( get_property( b, "resource:bucket_backend" ).getOrElse( "" ) == backend ) {
                    val extra_read = Json.toJson( Map(
                      "bucket" -> get_property( b, "resource:bucket_backend_id" ).getOrElse( "" ),
                      "name" -> ( get_property( d, "resource:path" ).getOrElse( "" ) + get_creation_time( v ).getOrElse( "" ) ),
                      "backend" -> backend
                    ) ).as[JsObject]
                    val extra_write = Json.toJson( Map(
                      "backend" -> backend,
                      "bucket" -> get_property( dest_bucket, "resource:bucket_backend_id" ).getOrElse( "" ),
                      "name" -> ( request.body.fileName + now.toString )
                    ) ).as[JsObject]
                    // Step 2: Request access authorization from Resource Manager
                    for {
                      auth_read <- rmc.authorize( AccessRequestFormat, request.body.underlyingRead.toAccessRequest( Some( extra_read ) ), token )
                      auth_write <- rmc.authorize( AccessRequestFormat, request.body.underlyingCreate( dest_bucket.id ).toAccessRequest( Some( extra_write ) ), token )
                      result <- {
                        // Step 3: Validate response from RM
                        for { ag_read <- auth_read; ag_write <- auth_write } yield {
                          if ( ag_read.verifyAccessToken( rmJwtVerifier.get ).extraClaims.contains( extra_read ) && ag_write.verifyAccessToken( rmJwtVerifier.get ).extraClaims.contains( extra_write ) ) {
                            backends.getBackend( backend ) match {
                              case Some( back ) => {
                                val duplicateResult = back.duplicateFile(
                                  request,
                                  get_property( b, "resource:bucket_backend_id" ).getOrElse( "" ),
                                  get_property( d, "resource:path" ).getOrElse( "" ) + get_creation_time( v ).getOrElse( "" ),
                                  get_property( dest_bucket, "resource:bucket_backend_id" ).getOrElse( "" ),
                                  request.body.fileName + now.toString
                                )
                                if ( duplicateResult ) {
                                  // Step 4: Log to KnowledgeGraph
                                  val fvertex = new NewVertexBuilder( 1 )
                                    .addSingleProperty( "resource:file_name", StringValue( request.body.fileName ) )
                                    .addSingleProperty( "resource:owner", StringValue( request.userId ) )
                                    .addLabels( request.body.labels )
                                    .addType( NamespaceAndName( "resource:file" ) )
                                    .result()
                                  val lvertex = new NewVertexBuilder( 2 )
                                    .addSingleProperty( "resource:path", StringValue( request.body.fileName ) )
                                    .addSingleProperty( "resource:owner", StringValue( request.userId ) )
                                    .addType( NamespaceAndName( "resource:file_location" ) )
                                    .result()
                                  val vvertex = new NewVertexBuilder( 3 )
                                    .addSingleProperty( "system:creation_time", LongValue( now ) )
                                    .addSingleProperty( "resource:owner", StringValue( request.userId ) )
                                    .addType( NamespaceAndName( "resource:file_version" ) )
                                    .result()
                                  val edges = Seq(
                                    NewEdge( NamespaceAndName( "resource:stored_in" ), Left( lvertex.tempId ), Right( b.id ), Map() ),
                                    NewEdge( NamespaceAndName( "resource:version_of" ), Left( vvertex.tempId ), Left( fvertex.tempId ), Map() ),
                                    NewEdge( NamespaceAndName( "resource:has_version" ), Left( lvertex.tempId ), Left( vvertex.tempId ), Map() ),
                                    NewEdge( NamespaceAndName( "resource:has_location" ), Left( fvertex.tempId ), Left( lvertex.tempId ), Map() )
                                  ) ++ request.executionId.map( eId => {
                                      Seq( NewEdge( NamespaceAndName( "resource:read" ), Right( eId ), Right( v.id ), Map() ) )
                                    } ).getOrElse( Seq() )

                                  val createAndWriteEdges = request.executionId.map { execId =>
                                    Seq(
                                      NewEdge( NamespaceAndName( "resource:write" ), Right( execId ), Left( vvertex.tempId ), Map() ),
                                      NewEdge( NamespaceAndName( "resource:create" ), Right( execId ), Left( vvertex.tempId ), Map() )
                                    )
                                  }.getOrElse( Seq.empty )
                                  val projectEdge = projectId.map { pid =>
                                    NewEdge( NamespaceAndName( "project:is_part_of" ), Left( fvertex.tempId ), Right( pid ), Map() )
                                  }
                                  val vertices = Seq( fvertex, lvertex, vvertex ).map( CreateVertexOperation )
                                  val allEdges = ( edges ++ createAndWriteEdges ++ projectEdge.toSeq ).map( CreateEdgeOperation )
                                  val mut = Mutation( vertices ++ allEdges ) // First vertex is the file vertex (used later)

                                  gc.postAndWait( mut ).map( ev => {
                                    val response = ev.status match {
                                      case EventStatus.Completed( res ) => res
                                      case EventStatus.Pending          => throw new RuntimeException( s"Expected completed mutation: ${ev.uuid}" )
                                    }

                                    val mutationResponse = response.event.as[MutationResponse]
                                    val fileVertexId = mutationResponse match {
                                      case MutationSuccess( results ) => results.head // First vertex is the file vertex
                                      case MutationFailed( reason )   => throw new RuntimeException( s"Bucket creation failed, caused by: $reason" )
                                    }

                                    Created( Json.toJson( ( fileVertexId ) ) )
                                  } )

                                }
                                else {
                                  logger.error( "Error in the backend" )
                                  Future( InternalServerError( "Error in the backend." ) )
                                }
                              }
                              case None => {
                                logger.error( s"Unknown backend $backend." )
                                Future( BadRequest( s"Unknown backend $backend." ) )
                              }
                            }
                          }
                          else {
                            logger.error( s"Resource Manager response is invalid. Got: $ag_read, $ag_write Expected extras: $extra_read, $extra_write" )
                            Future( InternalServerError( "Resource Manager response is invalid." ) )
                          }
                        }
                      }.getOrElse( Future( InternalServerError( "Resource Manager didn't respond." ) ) )
                    } yield { result }
                  }
                  else {
                    logger.error( "Cannot duplicate accross backends." )
                    Future( BadRequest( "Cannot duplicate accross backends." ) )
                  }
                }
              } yield { res }
            } ).getOrElse {
              logger.warn( s"Resource ${request.body.resourceId} not found" )
              Future( NotFound )
            }
          }
          else {
            Future( NotFound )
          }
        }
      }
      else {
        logger.info( s"Resource ${request.body.bucketId.getOrElse( "-" )} is not a bucket" )
        Future( NotFound )
      }
    }
  }

  def bucketCreate = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[CreateBucketRequest]( CreateBucketRequestFormat ) ) { implicit request =>
    logger.info( s"bucketCreate - ${request.body} - ${request.token.getSubject}" )

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
    val projectId = request.headers.get( "Renga-Projects-Project" ).map( _.toLong )

    rmc.authorize( AccessRequestFormat, request.body.toAccessRequest( extra ), token ).flatMap( res =>
      res.map( ag => if ( ag.verifyAccessToken( rmJwtVerifier.get ).extraClaims.equals( extra ) ) {
        backends.getBackend( backend ) match {
          case Some( back ) =>
            val bid = back.createBucket( request, name )
            val vertex = new NewVertexBuilder()
              .addSingleProperty( "resource:bucket_backend_id", StringValue( bid ) )
              .addSingleProperty( "resource:bucket_name", StringValue( name ) )
              .addSingleProperty( "resource:bucket_backend", StringValue( backend ) )
              .addSingleProperty( "resource:owner", StringValue( request.userId ) )
              .addLabels( request.body.labels )
              .addType( NamespaceAndName( "resource:bucket" ) )
              .result()
            val projectEdge = projectId.map { pid =>
              CreateEdgeOperation(
                NewEdge( NamespaceAndName( "project:is_part_of" ), Left( vertex.tempId ), Right( pid ), Map() )
              )
            }
            val mut = Mutation( Seq( CreateVertexOperation( vertex ) ) ++ projectEdge.toSeq )
            gc.postAndWait( mut ).map { ev =>
              val response = ev.status match {
                case EventStatus.Completed( res ) => res
                case EventStatus.Pending          => throw new RuntimeException( s"Expected completed mutation: ${ev.uuid}" )
              }

              val mutationResponse = response.event.as[MutationResponse]
              val bucketVertexId = mutationResponse match {
                case MutationSuccess( results ) => results.head
                case MutationFailed( reason )   => throw new RuntimeException( s"Bucket creation failed, caused by: $reason" )
              }

              Created( bucketVertexId )
            }

          case None =>
            logger.info( s"The backend $backend is not enabled" )
            Future( BadRequest( s"The backend $backend is not enabled." ) )
        }
      }
      else {
        logger.error( s"Resource Manager response is invalid. Got: $ag Expected extras: $extra" )
        Future( InternalServerError( "Resource Manager response is invalid." ) )
      } ).getOrElse {
        logger.error( s"No response from Resource Manager" )
        Future( InternalServerError( "No response from Resource Manager." ) )
      } )
  }

  private[this] implicit class BuilderCanAddLabels( builder: NewVertexBuilder ) {
    def addLabels( labels: Set[String] ): NewVertexBuilder = {
      labels.foldLeft( builder ) { ( b, label ) =>
        b.addSetProperty( NamespaceAndName( "annotation:label" ), StringValue( label ) )
      }
    }
  }
}
