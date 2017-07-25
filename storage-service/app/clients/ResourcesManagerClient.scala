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

import ch.datascience.service.models.resource.AccessGrant
import play.api.libs.json._
import play.api.libs.ws._
import ch.datascience.service.models.resource.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


class ResourcesManagerClient @Inject()(host: String)(implicit context: ExecutionContext, ws: WSClient) {

  def authorize[T](writer: Writes[T], rrequest: T)(implicit token: String): Future[Option[AccessGrant]] = {
    val request: WSRequest = ws.url(host + "/authorize")
      .withHeaders("Accept" -> "application/json", "Authorization" -> token)
      .withRequestTimeout(10000.millis)
    request.post(Json.toJson(rrequest)(writer)).map {
      response =>
        println(response.body)
        response.json.validate(AccessGrantFormat).asOpt
    }
  }
}

