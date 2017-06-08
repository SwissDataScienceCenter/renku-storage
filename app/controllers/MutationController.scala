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

import javax.inject.{Inject, Singleton}

import models.{RequestWorker, ResponseWorker}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * Created by johann on 07/06/17.
  */
@Singleton
class MutationController @Inject()(
  protected val requestWorker: RequestWorker,
  protected val responseWorker: ResponseWorker
) extends Controller {

  implicit lazy val ec: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

  def postMutation: Action[JsValue] = Action.async(BodyParsers.parse.json) { implicit request =>
    val query = request.body
    //TODO: get token from header
    val token: JsValue = Json.parse("""{ "my-token-is-empty": true }""")
    val event = JsObject(Seq(
      "query" -> query,
      "token" -> token
    ))

    for {
      event <- requestWorker.add(event)
    } yield {
      val json = JsObject(Seq(
        "uuid" -> JsString(event.uuid.toString),
        "event" -> event.event,
        "timestamp" -> JsString(event.created.atZone(java.time.ZoneId.systemDefault).toString)
      ))
      Ok(json)
    }
  }

}
