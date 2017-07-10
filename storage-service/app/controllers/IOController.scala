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

import akka.util.ByteString
import controllers.storageBackends.Backends
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.libs.streams._
import play.api.mvc._

import scala.collection.JavaConversions.asScalaBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex
import scala.util.Try


@Singleton
class IOController @Inject()(config: play.api.Configuration, val playSessionStore: PlaySessionStore, backends: Backends) extends Controller {

  private def getProfiles(implicit request: RequestHeader): List[CommonProfile] = {
    val webContext = new PlayWebContext(request, playSessionStore)
    val profileManager = new ProfileManager[CommonProfile](webContext)
    val profiles = profileManager.getAll(true)
    asScalaBuffer(profiles).toList
  }

  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r


  def objectRead = Action.async { implicit request =>
    Future {
      val profile = getProfiles(request).head
      val bucket = Try(profile.getAttribute("bucket").toString)
      val name = Try(profile.getAttribute("name").toString)
      val backend = Try(profile.getAttribute("backend").toString)

      backends.getBackend(backend.getOrElse("")) match {
        case Some(back) =>
          (for { n <- name; b <- bucket } yield
              back.read(request, b, n) match {
                case Some(dataContent) => Ok.chunked(dataContent)
                case None => NotFound
              }).getOrElse(BadRequest)
        case None => BadRequest(s"The backend $backend is not enabled.")
      }
    }
  }

  def objectWrite = Action(forward()) { request =>
    request.body
  }

  def forward(): BodyParser[Result] = BodyParser { req =>
    Accumulator.source[ByteString].mapFuture { source =>
      Future {
        val profile = getProfiles(req).head
        val bucket = Try(profile.getAttribute("bucket").toString)
        val name = Try(profile.getAttribute("name").toString)
        val backend = Try(profile.getAttribute("backend").toString)

        backends.getBackend(backend.getOrElse("")) match {
          case Some(back) =>
            (for {n <- name; b <- bucket} yield
                  Right(
                    if (back.write(req, b, n, source))
                      Created
                    else
                      NotFound
                  )
              ).getOrElse(Right(BadRequest))
          case None => Right(BadRequest(s"The backend $backend is not enabled."))
        }
      }
    }
  }

  def bucketCreate = Action.async { implicit request =>
    Future {
      val profile = getProfiles(request).head
      val bucket = Try(profile.getAttribute("bucket").toString)
      val backend = Try(profile.getAttribute("backend").toString)

      backends.getBackend(backend.getOrElse("")) match {
        case Some(back) =>
          bucket.map( b =>
            if (back.createBucket(request, b))
              Created
            else
              Conflict
          ).getOrElse(BadRequest)
        case None => BadRequest(s"The backend $backend is not enabled.")
      }
    }
  }
}