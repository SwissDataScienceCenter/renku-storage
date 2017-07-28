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
import javax.inject._

import authorization.ResourcesManagerJWTVerifierProvider
import ch.datascience.service.models.resource.{ AccessGrant, ScopeQualifier }
import ch.datascience.service.security.{ ProfileFilterAction, TokenFilter }
import controllers.storageBackends.Backends
import play.api.libs.json.JsObject
import play.api.libs.streams._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex

@Singleton
class IOController @Inject() ( config: play.api.Configuration, backends: Backends, jwtVerifier: ResourcesManagerJWTVerifierProvider ) extends Controller {

  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r

  def objectRead = ProfileFilterAction( jwtVerifier.get ).async { implicit request =>

    val accessGrant = AccessGrant( request.token.getToken )
    val token = accessGrant.verifyAccessToken( jwtVerifier.get )

    val scope = token.scope
    val readRequest = token.extraClaims.get.as[JsObject]

    //TODO: check token content here.
    if ( !scope.contains( ScopeQualifier.StorageRead ) )
      Future.successful( Forbidden( s"Wrong scope" ) )
    else {
      Future {
        val bucket = ( readRequest \ "bucket" ).as[String]
        val name = ( readRequest \ "name" ).as[String]
        val backend = ( readRequest \ "backend" ).as[String]

        backends.getBackend( backend ) match {
          case Some( back ) =>
            back.read( request, bucket, name ) match {
              case Some( dataContent ) => Ok.chunked( dataContent )
              case None                => NotFound
            }
          case None => BadRequest( s"The backend $backend is not enabled." )
        }
      }
    }
  }

  def objectWrite = EssentialAction { reqh =>
    TokenFilter( jwtVerifier.get, "" ).filter( reqh ) match {
      case Right( profile ) =>
        val accessGrant = AccessGrant( profile.getToken )
        val token = accessGrant.verifyAccessToken( jwtVerifier.get )
        val scope = token.scope
        val writeRequest = token.extraClaims.get.as[JsObject]
        println( "Write request" )
        println( writeRequest )

        //TODO: check token scope here, Write or Create (see objectRead)
        //TODO: check token content here

        val bucket = ( writeRequest \ "bucket" ).as[String]
        val name = ( writeRequest \ "name" ).as[String]
        val backend = ( writeRequest \ "backend" ).as[String]
        backends.getBackend( backend ) match {
          case Some( back ) =>
            back.write( reqh, bucket, name )
          case None => Accumulator.done( BadRequest( s"The backend $backend is not enabled." ) )
        }
      case Left( res ) => Accumulator.done( res )
    }
  }
}
