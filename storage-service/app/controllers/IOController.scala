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
import ch.datascience.service.security.{ProfileFilterAction, TokenFilter}
import controllers.storageBackends.Backends
import play.api.libs.streams._
import play.api.mvc._
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex


@Singleton
class IOController @Inject()(config: play.api.Configuration, backends: Backends, jwtVerifier: ResourcesManagerJWTVerifierProvider) extends Controller {


  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r


  def objectRead = ProfileFilterAction(jwtVerifier.get).async { implicit request =>
    Future {
      val extra = Json.parse(request.token.getClaim("resource_extras").asString()).as[Map[String, String]]

      (for {
        backend <- extra.get("backend")
        back <- backends.getBackend(backend)
        bucket <- extra.get("bucket")
        name <- extra.get("name")
        datacontent <- back.read(request, bucket, name)
      } yield {
        Ok.chunked(datacontent)
      }).getOrElse(NotFound)
    }
  }

  def objectWrite = EssentialAction { reqh =>
        TokenFilter(jwtVerifier.get, "").filter(reqh) match {
          case Right(profile) =>
            val extra = Json.parse(profile.getClaim("resource_extras").asString()).as[Map[String, String]]
            (for {
              backend <- extra.get("backend")
              back <- backends.getBackend(backend)
              bucket <- extra.get("bucket")
              name <- extra.get("name")
            } yield {
                  back.write(reqh, bucket, name)
            }).getOrElse(Accumulator.done(NotFound))
          case Left(res) => Accumulator.done(res)
        }
      }
}