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

package ch.datascience.graph.types.persistence.model.json

import java.util.UUID

import play.api.libs.json._

/**
  * Created by johann on 13/04/17.
  */
object UUIDMappers {

  lazy val UUIDFormat: Format[UUID] = Format(uuidReads, uuidWrites)

  private[this] def uuidWrites: Writes[UUID] = new Writes[UUID] {
    def writes(uuid: UUID): JsString = JsString(uuid.toString)
  }

  private[this] def uuidReads: Reads[UUID] = new Reads[UUID] {
    def reads(json: JsValue): JsResult[UUID] = json.validate[String] flatMap { str =>
      try {
        JsSuccess(UUID.fromString(str))
      } catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }
  }

  lazy val notUUidReads: Reads[String] = new Reads[String] {
    def reads(json: JsValue): JsResult[String] = json.validate[UUID] match {
      case JsSuccess(uuid, _) => JsError(s"UUID string forbidden: $uuid")
      case JsError(_) => json.validate[String]
    }
  }

}
