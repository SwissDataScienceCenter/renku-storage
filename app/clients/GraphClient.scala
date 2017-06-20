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

import ch.datascience.graph.elements.mutation.Mutation
import models.json._

import scala.concurrent.Future
import scala.concurrent.duration._
import play.api.libs.json._
import play.api.libs.ws._

import scala.concurrent.ExecutionContext

class GraphClient @Inject() (implicit context: ExecutionContext, ws: WSClient, host: String) {

  def status(id: String): Future[JsValue] = {
    val request: WSRequest = ws.url(host + "/mutation/" + id)
      .withHeaders("Accept" -> "application/json")
      .withRequestTimeout(10000.millis)
    val futureResult: Future[JsValue] = request.get().map {
      response =>
        response.json
    }
    futureResult
  }

  def create(mutation: Mutation): Future[JsValue] = {

    val request: WSRequest = ws.url(host + "/mutation")
      .withHeaders("Accept" -> "application/json")
      .withRequestTimeout(10000.millis)
    val futureResult: Future[JsValue] = request.post(Json.toJson(mutation)).map {
      response =>
        response.json
    }
    futureResult
  }
}

