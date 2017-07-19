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

import javax.inject._

import authorization.ResourcesManagerJWTVerifierProvider
import ch.datascience.service.security.{ProfileFilterAction, TokenFilterAction}
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import controllers.storageBackends.Backends
import play.api.libs.streams._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex


@Singleton
class IOController @Inject()(config: play.api.Configuration, backends: Backends, jwtVerifier: ResourcesManagerJWTVerifierProvider) extends Controller {

  private def getProfile(request: RequestHeader): Option[DecodedJWT] = {

    val tokenRegexp: Regex = "(?i)Bearer (.*)/i".r.anchored

    request.headers.get("Authorization") match {
      case Some(header) =>  header match {
        case tokenRegexp(token) => Some(JWT.decode(token))
        case _ => None
      }
      case None => None
    }
  }

  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r


  def objectRead = ProfileFilterAction(jwtVerifier.get).async { implicit request =>
    Future {
      val bucket = request.token.getClaim("bucket").asString()
      val name = request.token.getClaim("name").asString()
      val backend = request.token.getClaim("backend").asString()

      backends.getBackend(backend) match {
        case Some(back) =>
              back.read(request, bucket, name) match {
                case Some(dataContent) => Ok.chunked(dataContent)
                case None => NotFound
              }
        case None => BadRequest(s"The backend $backend is not enabled.")
      }
    }
  }

  def objectWrite = EssentialAction { reqh =>
        TokenFilterAction(jwtVerifier.get).filter(reqh) match {
          case Right(req) =>
            val profile = getProfile(req)
            val bucket = profile.map(p => p.getClaim("bucket").asString())
            val name = profile.map(p => p.getClaim("name").asString())
            val backend = profile.map(p => p.getClaim("backend").asString())

            backends.getBackend (backend.getOrElse ("") ) match {
              case Some (back) =>
                (for {
                  n <- name
                  b <- bucket
                } yield
                  back.write (req, b, n)
                ).getOrElse (Accumulator.done (BadRequest) )
              case None => Accumulator.done (BadRequest (s"The backend $backend is not enabled.") )
            }
          case Left(res) => Accumulator.done(res)
        }
      }
}