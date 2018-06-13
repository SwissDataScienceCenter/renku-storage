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

package models

import java.time.Instant

import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, JsValue, OFormat }

case class Event(
    id:      Option[Long],
    obj:     JsValue,
    action:  String,
    attr:    JsValue,
    created: Instant
)

object Event {

  def format: OFormat[Event] = (
    ( JsPath \ "id" ).formatNullable[Long] and
    ( JsPath \ "obj" ).format[JsValue] and
    ( JsPath \ "action" ).format[String] and
    ( JsPath \ "attr" ).format[JsValue] and
    ( JsPath \ "created" ).format[Instant]
  )( read, write )

  private[this] def read(
      id:      Option[Long],
      obj:     JsValue,
      action:  String,
      attr:    JsValue,
      created: Instant
  ): Event = {
    Event(
      id, obj, action, attr, created
    )
  }

  private[this] def write( request: Event ): ( Option[Long], JsValue, String, JsValue, Instant ) = {
    ( request.id, request.obj, request.action, request.attr, request.created )
  }
}
