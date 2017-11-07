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
import ch.datascience.graph.elements.mutation.update.UpdateVertexPropertyOperation
import ch.datascience.graph.elements.mutation.{ GraphMutationClient, Mutation }
import ch.datascience.graph.elements.persisted.PersistedVertex
import ch.datascience.graph.naming.NamespaceAndName
import ch.datascience.graph.values.{ LongValue, StringValue }
import ch.datascience.service.ResourceManagerClient
import ch.datascience.service.models.resource.json._
import ch.datascience.service.models.storage.json._
import ch.datascience.service.models.storage.WriteResourceRequest
import ch.datascience.service.security.ProfileFilterAction
import ch.datascience.service.utils.persistence.graph.{ GraphExecutionContextProvider, JanusGraphTraversalSourceProvider }
import ch.datascience.service.utils.persistence.reader.VertexReader
import ch.datascience.service.utils.{ ControllerWithBodyParseJson, ControllerWithGraphTraversal }
import models.RenameRequest
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.Controller

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

  def get_creation_time( persistedVertex: PersistedVertex ) =
    persistedVertex.properties.get( NamespaceAndName( "system:creation_time" ) ).flatMap( v => v.values.headOption.map( value => value.asInstanceOf[LongValue].self ) )

  def objectRename( fileId: Long ) = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[RenameRequest]( RenameRequest.format ) ) { implicit request =>
    logger.info( s"objectRename - ${request.body} - ${request.token.getSubject}" )

    /* Steps:
 *   1. Resolve graph entities
 *   2. Request access authorization from Resource Manager
 *   3. Validate response from RM
 *   4. Log to Knowledge Graph
 */

    // Step 1: Resolve graph entities
    val token: String = request.headers.get( "Authorization" ).getOrElse( "" )
    val rmc = new ResourceManagerClient( config )
    val g = graphTraversalSource
    val t = g.V( Long.box( fileId ) ).out( "resource:has_location" )
    graphExecutionContext.execute {
      if ( t.hasNext ) {
        vertexReader.read( t.next() ).flatMap { data =>
          val extra = Some( Json.toJson( Map(
            "old_name" -> get_property( data, "resource:path" ),
            "new_name" -> Some( request.body.newFileName )
          ) ).as[JsObject] )
          val resourceRequest = WriteResourceRequest( fileId )
          // Step 2: Request access authorization from Resource Manager
          rmc.authorize( AccessRequestFormat, resourceRequest.toAccessRequest( extra ), token ).flatMap( ret => {
            // Step 3: Validate response from RM
            ret.map( ag =>
              if ( ag.verifyAccessToken( rmJwtVerifier.get ).extraClaims.equals( extra ) ) {
                // Step 4: Log to KnowledgeGraph
                val update = UpdateVertexPropertyOperation( data.properties( NamespaceAndName( "resource:path" ) ).head, StringValue( request.body.newFileName ) )
                val mut = Mutation( Seq( update ) )
                gc.postAndWait( mut ).map( ev => Ok( s"Renamed file $fileId to ${request.body.newFileName}" ) )
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
        }
      }
      else
        Future( NotFound )
    }
  }

}
