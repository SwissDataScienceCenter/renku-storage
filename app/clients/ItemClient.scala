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

package clients
import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.duration._
import play.api.libs.json._
import play.api.libs.ws._

import scala.concurrent.ExecutionContext

class ItemClient @Inject() (implicit context: ExecutionContext, ws: WSClient, host: String) {

  def list = {
    val request: WSRequest = ws.url(host + "/item")
      .withHeaders("Accept" -> "application/models.json")
      .withRequestTimeout(10000.millis)
    val futureResult: Future[String] = request.get().map {
      response =>
        response.json.as[String] //you can also map it back to objects
    }
  }

  def create = {

    val item = Json.obj(
      "key1" -> "value1",
      "key2" -> "value2"
    )
    val request: WSRequest = ws.url(host + "/item")
      .withHeaders("Accept" -> "application/models.json")
      .withRequestTimeout(10000.millis)
    val futureResult: Future[String] = request.post(item).map {
      response =>
        response.json.as[String] //you can also map it back to objects
    }
  }

  def update(id: Long) = {
    val item = Json.obj(
      "key1" -> "value1",
      "key2" -> "value2"
    )
    val request: WSRequest = ws.url(host + "/item")
      .withHeaders("Accept" -> "application/models.json")
      .withRequestTimeout(10000.millis)
      .withQueryString("id" -> id.toString)
    val futureResult: Future[String] = request.post(item).map {
      response =>
        response.json.as[String] //you can also map it back to objects
    }
  }

  def details(id: Long) = {
    val request: WSRequest = ws.url(host + "/item")
      .withHeaders("Accept" -> "application/models.json")
      .withRequestTimeout(10000.millis)
      .withQueryString("id" -> id.toString)
    val futureResult: Future[String] = request.get().map {
      response =>
        response.json.as[String] //you can also map it back to objects
    }
  }
}

