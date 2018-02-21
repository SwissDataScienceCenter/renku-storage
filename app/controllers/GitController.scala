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

import java.util.UUID
import javax.inject.{ Inject, Singleton }

import authorization.{ JWTVerifierProvider, ResourcesManagerJWTVerifierProvider }
import ch.datascience.graph.elements.mutation.create.{ CreateEdgeOperation, CreateVertexOperation }
import ch.datascience.graph.elements.mutation.log.model.EventStatus
import ch.datascience.graph.elements.mutation.{ GraphMutationClient, Mutation }
import ch.datascience.graph.elements.new_.NewEdge
import ch.datascience.graph.elements.new_.build.NewVertexBuilder
import ch.datascience.graph.elements.persisted.PersistedVertex
import ch.datascience.graph.naming.NamespaceAndName
import ch.datascience.graph.values.{ LongValue, StringValue }
import ch.datascience.service.ResourceManagerClient
import ch.datascience.service.models.resource.{ SingleScopeAccessRequest, SingleScopeResourceAccessRequest }
import ch.datascience.service.models.resource.json.AccessRequestFormat
import ch.datascience.service.models.storage.{ CreateFileRequest, ReadResourceRequest }
import ch.datascience.service.security.{ ProfileFilterAction, TokenFilter }
import ch.datascience.service.utils.persistence.graph.{ GraphExecutionContextProvider, JanusGraphTraversalSourceProvider }
import ch.datascience.service.utils.persistence.reader.VertexReader
import ch.datascience.service.utils.ControllerWithGraphTraversal
import controllers.storageBackends.{ Backends, GitBackend }
import models._
import models.persistence.DatabaseLayer
import org.apache.tinkerpop.gremlin.structure.Vertex
import play.api.Logger

import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.streams.Accumulator
import play.api.libs.ws._
import play.api.mvc._
import utils.ControllerWithBodyParseTolerantJson

import scala.concurrent.{ Await, Future }
/**
 * Created by jeberle on 25.04.17.
 */
@Singleton
class GitController @Inject() (
    config:                                         play.api.Configuration,
    jwtVerifier:                                    JWTVerifierProvider,
    backends:                                       Backends,
    rmJwtVerifier:                                  ResourcesManagerJWTVerifierProvider,
    graphMutationClientProvider:                    GraphMutationClientProvider,
    implicit val wsclient:                          WSClient,
    protected val orchestrator:                     DatabaseLayer,
    implicit val graphExecutionContextProvider:     GraphExecutionContextProvider,
    implicit val janusGraphTraversalSourceProvider: JanusGraphTraversalSourceProvider,
    implicit val vertexReader:                      VertexReader
) extends Controller with ControllerWithBodyParseTolerantJson with ControllerWithGraphTraversal with RequestHelper {

  lazy val gc: GraphMutationClient = graphMutationClientProvider.get

  lazy val logger: Logger = Logger( "application.GitController" )

  val host: String = config.getString( "renga_host" ).get

  implicit lazy val LFSBatchResponseFormat: OFormat[LFSBatchResponse] = LFSBatchResponse.format
  implicit lazy val LFSBatchResponseUpFormat: OFormat[LFSBatchResponseUp] = LFSBatchResponseUp.format

  def getRefs( id: String ) = ProfileFilterAction( jwtVerifier.get ).async { implicit request =>

    val json = JsString( id )
    json.validate[UUID] match {
      case JsError( e ) => Future( BadRequest( JsError.toJson( e ) ) )
      case JsSuccess( uuid, _ ) =>
        orchestrator.repositories.findByUUID( uuid ).flatMap {
          case Some( repo ) =>
            val backend = repo.backend
            backends.getBackend( backend ) match {
              case Some( back ) =>
                back.asInstanceOf[GitBackend].getRefs( request, repo.path, request.userId )
              case None => Future.successful( BadRequest( s"The backend $backend is not enabled." ) )
            }
          case None => Future.successful( NotFound )
        }
    }
  }

  def uploadPack( id: String ) = EssentialAction { reqh =>
    TokenFilter( jwtVerifier.get, "" ).filter( reqh ) match {
      case Right( profile ) =>
        val json = JsString( id )
        val futur = json.validate[UUID] match {
          case JsError( e ) => Future( Accumulator.done( BadRequest( JsError.toJson( e ) ) ) )
          case JsSuccess( uuid, _ ) =>
            orchestrator.repositories.findByUUID( uuid ).map {
              case Some( repo ) =>
                val backend = repo.backend
                backends.getBackend( backend ) match {
                  case Some( back ) =>
                    back.asInstanceOf[GitBackend].upload( reqh, repo.path, "" ) //profile.getId )
                  case None => Accumulator.done( BadRequest( s"The backend $backend is not enabled." ) )
                }
              case None => Accumulator.done( NotFound )
            }
        }
        Await.result( futur, 10.seconds )
      case Left( res ) => Accumulator.done( res )
    }
  }

  def receivePack( id: String ) = EssentialAction { reqh =>
    TokenFilter( jwtVerifier.get, "" ).filter( reqh ) match {
      case Right( profile ) =>
        val json = JsString( id )
        val futur = json.validate[UUID] match {
          case JsError( e ) => Future( Accumulator.done( BadRequest( JsError.toJson( e ) ) ) )
          case JsSuccess( uuid, _ ) =>
            orchestrator.repositories.findByUUID( uuid ).map {
              case Some( repo ) =>
                val backend = repo.backend
                backends.getBackend( backend ) match {
                  case Some( back ) =>
                    back.asInstanceOf[GitBackend].receive( reqh, repo.path, profile.getId )
                  case None => Accumulator.done( BadRequest( s"The backend $backend is not enabled." ) )
                }
              case None => Accumulator.done( NotFound )
            }
        }
        Await.result( futur, 10.seconds )
      case Left( res ) => Accumulator.done( res )
    }
  }

  def get_property( persistedVertex: PersistedVertex, name: String ) =
    persistedVertex.properties.get( NamespaceAndName( name ) ).flatMap( v => v.values.headOption.map( value => value.asInstanceOf[StringValue].self ) )

  def get_creation_time( persistedVertex: PersistedVertex ) =
    persistedVertex.properties.get( NamespaceAndName( "system:creation_time" ) ).flatMap( v => v.values.headOption.map( value => value.asInstanceOf[LongValue].self ) )

  def lfsBatch( id: String ): Action[LFSBatchRequest] = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[LFSBatchRequest]( LFSBatchRequest.format ) ) { implicit request =>
    val token: String = request.headers.get( "Authorization" ).getOrElse( "" )
    val rmc = new ResourceManagerClient( config )

    if ( request.body.operation == "download" ) {

      val objects = request.body.objects.map( lfsObject => {
        val g = graphTraversalSource
        val t = g.V().has( "type", "resource:file_version" ).has( "resource:file_hash", lfsObject.oid ).as( "version" )
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
                  rmc.authorize( AccessRequestFormat, SingleScopeResourceAccessRequest( v.id, ReadResourceRequest.scope, extra ), token ).flatMap( ret => {
                    // Step 3: Validate response from RM
                    Future( ret.flatMap( ag => if ( ag.verifyAccessToken( rmJwtVerifier.get ).extraClaims.equals( extra ) ) {
                      request.executionId.map( eId => {
                        // Step 4: Log to KnowledgeGraph
                        val edge = NewEdge( NamespaceAndName( "resource:read" ), Right( eId ), Right( v.id ), Map() )
                        val mut = Mutation( Seq( CreateEdgeOperation( edge ) ) )
                        gc.postAndWait( mut )
                      } //TODO: maybe take into account if the node was created or not
                      // Step 5: Send authorization to client
                      )
                      Some( LFSObjectResponse( lfsObject.oid, lfsObject.size, true, Some( LFSDownload( host + "/api/storage/io/read", "Bearer " + ag.accessToken, 600 ) ) ) )
                    }
                    else {
                      logger.error( s"Resource Manager response is invalid. Got: $ag Expected extras: $extra" )
                      None
                    } ) )
                  } )
                } )
              } )
            } ).getOrElse( Future( None ) )
          }
          else
            Future( None )
        }
      } )
      Future.sequence( objects ).map( l => Ok( Json.toJson( LFSBatchResponse( request.body.transfers, l.filter( _.nonEmpty ).map( _.get ) ) ) ) )
    }
    else {
      val objects = request.body.objects.map( lfsObject => {
        val g = graphTraversalSource
        val t = g.V().has( "type", "resource:file_version" ).has( "resource:file_hash", lfsObject.oid )
        val now = System.currentTimeMillis
        if ( !graphExecutionContext.execute { t.hasNext } ) {
          getVertexByType( "resource:bucket" ).flatMap {
            case Some( vertex ) => {
              val backend = get_property( vertex, "resource:bucket_backend" ).getOrElse( "" )
              val extra = Some( Json.toJson( Map(
                "bucket" -> get_property( vertex, "resource:bucket_backend_id" ).getOrElse( "" ),
                "name" -> ( lfsObject.oid + now.toString ),
                "backend" -> backend
              ) ).as[JsObject] )
              // Step 2: Request access authorization from Resource Manager
              rmc.authorize( AccessRequestFormat, SingleScopeAccessRequest( permissionHolderId = None, CreateFileRequest.scope, extra ), token ).flatMap( ret => {
                // Step 3: Validate response from RM
                Future( ret.flatMap( ag => if ( ag.verifyAccessToken( rmJwtVerifier.get ).extraClaims.equals( extra ) ) {
                  val fvertex = new NewVertexBuilder( 1 )
                    .addSingleProperty( "resource:file_name", StringValue( lfsObject.oid ) )
                    .addSingleProperty( "resource:owner", StringValue( request.userId ) )
                    .addType( NamespaceAndName( "resource:file" ) )
                    .result()
                  val lvertex = new NewVertexBuilder( 2 )
                    .addSingleProperty( "resource:path", StringValue( lfsObject.oid ) )
                    .addSingleProperty( "resource:owner", StringValue( request.userId ) )
                    .addType( NamespaceAndName( "resource:file_location" ) )
                    .result()
                  val vvertex = new NewVertexBuilder( 3 )
                    .addSingleProperty( "system:creation_time", LongValue( now ) )
                    .addSingleProperty( "resource:file_hash", StringValue( lfsObject.oid ) )
                    .addSingleProperty( "resource:file_size", StringValue( lfsObject.size.toString ) )
                    .addSingleProperty( "resource:owner", StringValue( request.userId ) )
                    .addType( NamespaceAndName( "resource:file_version" ) )
                    .result()
                  val edges = Seq(
                    NewEdge( NamespaceAndName( "resource:stored_in" ), Left( lvertex.tempId ), Right( vertex.id ), Map() ),
                    NewEdge( NamespaceAndName( "resource:version_of" ), Left( vvertex.tempId ), Left( fvertex.tempId ), Map() ),
                    NewEdge( NamespaceAndName( "resource:has_version" ), Left( lvertex.tempId ), Left( vvertex.tempId ), Map() ),
                    NewEdge( NamespaceAndName( "resource:has_location" ), Left( fvertex.tempId ), Left( lvertex.tempId ), Map() )
                  )
                  val vertices = Seq( fvertex, lvertex, vvertex ).map( CreateVertexOperation )
                  val allEdges = edges.map( CreateEdgeOperation )
                  val mut = Mutation( vertices ++ allEdges ) // First vertex is the file vertex (used later)
                  //TODO: maybe take into account if the node was created or not
                  gc.postAndWait( mut ).map { ev =>
                    val response = ev.status match {
                      case EventStatus.Completed( res ) => res
                      case EventStatus.Pending          => throw new RuntimeException( s"Expected completed mutation: ${ev.uuid}" )
                    }
                  }
                  Some( LFSObjectResponseUp( lfsObject.oid, lfsObject.size, true, Some( LFSUpload( host + "/api/storage/io/write", "Bearer " + ag.accessToken, 600 ) ) ) )
                }
                else {
                  logger.error( s"Resource Manager response is invalid. Got: $ag Expected extras: $extra" )
                  None
                } ) )
              } )
            }
            case _ => Future.successful( None )
          }
        }
        else
          Future.successful( Some( LFSObjectResponseUp( lfsObject.oid, lfsObject.size, true, None ) ) )
      } )
      Future.sequence( objects ).map( l => Ok( Json.toJson( LFSBatchResponseUp( request.body.transfers, l.filter( _.nonEmpty ).map( _.get ) ) ) ) )
    }
  }

}

