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

package models.json

import models.DeployResult
import play.api.libs.json._
import play.api.libs.functional.syntax._

object DeployResultMappers {

  def deployResultWrite: Writes[DeployResult] = (
    (JsPath \ "id").write[String] and
    (JsPath \ "success").write[Boolean] and
    (JsPath \ "ports-forwarding").write[Map[String, String]] and
    (JsPath \ "images").write[List[String]] and
    (JsPath \ "message").write[String]
  )(unlift(DeployResult.unapply))

}
