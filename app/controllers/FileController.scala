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
import ch.datascience.graph.elements.mutation.create.CreateVertexPropertyOperation
import ch.datascience.graph.elements.mutation.delete.DeleteVertexPropertyOperation
import ch.datascience.graph.elements.mutation.log.model.json._
import ch.datascience.graph.elements.mutation.log.model.{ EventStatus, MutationFailed, MutationResponse, MutationSuccess }
import ch.datascience.graph.elements.mutation.update.UpdateVertexPropertyOperation
import ch.datascience.graph.elements.mutation.{ GraphMutationClient, Mutation, Operation }
import ch.datascience.graph.elements.new_.NewRichProperty
import ch.datascience.graph.elements.persisted.{ PersistedVertex, VertexPath }
import ch.datascience.graph.naming.NamespaceAndName
import ch.datascience.graph.values.StringValue
import ch.datascience.service.ResourceManagerClient
import ch.datascience.service.models.resource.json._
import ch.datascience.service.models.storage.WriteResourceRequest
import ch.datascience.service.security.{ ProfileFilterAction, RequestWithProfile }
import ch.datascience.service.utils.persistence.graph.{ GraphExecutionContextProvider, JanusGraphTraversalSourceProvider }
import ch.datascience.service.utils.persistence.reader.VertexReader
import ch.datascience.service.utils.{ ControllerWithBodyParseJson, ControllerWithGraphTraversal }
import models.FileUpdateRequest
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.Future

/**
 * Created by jeberle on 25.04.17.
 */
@Singleton
class FileController @Inject() (
    config:                                         play.api.Configuration,
    jwtVerifier:                                    JWTVerifierProvider,
    rmJwtVerifier:                                  ResourcesManagerJWTVerifierProvider,
    graphMutationClientProvider:                    GraphMutationClientProvider,
    implicit val wsclient:                          WSClient,
    implicit val graphExecutionContextProvider:     GraphExecutionContextProvider,
    implicit val janusGraphTraversalSourceProvider: JanusGraphTraversalSourceProvider,
    implicit val vertexReader:                      VertexReader
) extends Controller with ControllerWithBodyParseJson with ControllerWithGraphTraversal with RequestHelper {

  lazy val gc: GraphMutationClient = graphMutationClientProvider.get

  lazy val logger: Logger = Logger( "application.AuthorizeController" )

  def get_property( persistedVertex: PersistedVertex, name: String ) =
    persistedVertex.properties.get( NamespaceAndName( name ) ).flatMap( v => v.values.headOption.map( value => value.asInstanceOf[StringValue].self ) )

  def fileUpdate( fileId: Long ): Action[FileUpdateRequest] = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[FileUpdateRequest]( FileUpdateRequest.format ) ) { implicit request =>
    logger.info( s"fileUpdate - ${request.body} - ${request.token.getSubject}" )
    objectUpdate( fileId, request, "resource:file", "resource:file_name" )
  }

  def bucketUpdate( bucketId: Long ): Action[FileUpdateRequest] = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[FileUpdateRequest]( FileUpdateRequest.format ) ) { implicit request =>
    logger.info( s"bucketUpdate - ${request.body} - ${request.token.getSubject}" )
    objectUpdate( bucketId, request, "resource:bucket", "resource:bucket_name" )
  }

  /* Steps:
     *   1. Resolve graph entities
     *   2. Request access authorization from Resource Manager
     *   3. Validate response from RM
     *   4. Log to Knowledge Graph
     */

  def objectUpdate( objectId: Long, request: RequestWithProfile[FileUpdateRequest], oType: String, attribute: String ) = {

    if ( request.body.fileName.isEmpty && request.body.labels.isEmpty ) {
      // Nothing to update
      Future( Ok( JsObject( Seq( "message" -> JsString( "No update" ) ) ) ) )
    }
    else {
      // Step 1: Resolve graph entities
      val token: String = request.headers.get( "Authorization" ).getOrElse( "" )
      val rmc = new ResourceManagerClient( config )
      val g = graphTraversalSource
      val t = g.V( Long.box( objectId ) ).has( "type", oType )
      graphExecutionContext.execute {
        if ( t.hasNext ) {
          vertexReader.read( t.next() ).flatMap { vertex =>
            val extra = Some(
              JsObject(
                request.body.fileName.map { x => "file_name" -> JsString( x ) }.toSeq
                  ++ request.body.labels.map { x => "labels" -> JsArray( x.map( JsString ).toSeq ) }.toSeq
              )
            )
            val resourceRequest = WriteResourceRequest( objectId )
            // Step 2: Request access authorization from Resource Manager
            rmc.authorize( AccessRequestFormat, resourceRequest.toAccessRequest( extra ), token ).flatMap( ret => {
              // Step 3: Validate response from RM
              ret.map( ag =>
                if ( ag.verifyAccessToken( rmJwtVerifier.get ).extraClaims.equals( extra ) ) {
                  // Step 4: Log to KnowledgeGraph
                  val ops = objectNameUpdate( request.body.fileName, vertex, attribute ).toSeq ++ labelsUpdate( request.body.labels, vertex )
                  if ( ops.isEmpty )
                    Future.successful( Ok( JsObject( Seq( "message" -> JsString( "Nothing to update" ) ) ) ) )
                  else {
                    val mut = Mutation( ops )
                    //gc.postAndWait( mut ).map( ev => Ok( s"Renamed file $fileId to ${request.body.fileName}" ) )
                    gc.postAndWait( mut ).map { ev =>
                      val response = ev.status match {
                        case EventStatus.Completed( res ) => res
                        case EventStatus.Pending          => throw new RuntimeException( s"Expected completed mutation: ${ev.uuid}" )
                      }

                      val mutationResponse = response.event.as[MutationResponse]
                      val vertexId = mutationResponse match {
                        case MutationSuccess( results ) => results.head
                        case MutationFailed( reason )   => throw new RuntimeException( s"File update failed, caused by: $reason" )
                      }

                      Ok( JsObject( Seq( "message" -> JsString( s"Applied update: ${request.body}" ) ) ) )
                    }
                  }

                } //TODO: maybe take into account if the node was created or not
                else {
                  logger.error( s"Resource Manager response is invalid. Got: $ag Expected extras: $extra" )
                  Future( InternalServerError( "Resource Manager response is invalid." ) )
                } ).getOrElse {
                logger.error( s"No response from Resource Manager" )
                Future( InternalServerError( "No response from Resource Manager." ) )
              }
            } )
          }
        }
        else
          Future( NotFound )
      }
    }
  }

  protected def objectNameUpdate( objectName: Option[String], vertex: PersistedVertex, attribute: String ): Option[UpdateVertexPropertyOperation] = {
    objectName.map { fn =>
      UpdateVertexPropertyOperation( vertex.properties( NamespaceAndName( attribute ) ).head, StringValue( fn ) )
    }
  }

  protected def labelsUpdate( labels: Option[Set[String]], vertex: PersistedVertex ): Seq[Operation] = labels match {
    case Some( ls ) =>
      val oldLabels = ( for {
        prop <- vertex.properties.getOrElse( NamespaceAndName( "annotation:label" ), Seq.empty )
        label = prop.value.unboxAs[String]
      } yield label -> prop ).toMap

      val newLabels = ls -- oldLabels.keySet
      val createOps = for {
        l <- newLabels.toSeq
      } yield {
        CreateVertexPropertyOperation( NewRichProperty( VertexPath( vertex.id ), NamespaceAndName( "annotation:label" ), StringValue( l ), Map.empty ) )
      }

      val deleteLabels = for { ( label, prop ) <- oldLabels if !ls.contains( label ) } yield prop
      val deleteOps = for {
        prop <- deleteLabels
      } yield DeleteVertexPropertyOperation( prop )

      createOps ++ deleteOps
    case None => Seq.empty
  }

}
