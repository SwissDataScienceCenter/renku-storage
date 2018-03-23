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
import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, OFormat }

case class FileObject(
    uuid:        UUID,
    description: String,
    name:        String,
    hash:        String,
    created:     Option[Instant],
    owner:       UUID
)

object FileObject {

  def format: OFormat[FileObject] = (
    ( JsPath \ "uuid" ).format[UUID] and
    ( JsPath \ "description" ).format[String] and
    ( JsPath \ "name" ).format[String] and
    ( JsPath \ "hash" ).format[String] and
    ( JsPath \ "created" ).formatNullable[Instant] and
    ( JsPath \ "owner" ).format[UUID]
  )( read, write )

  private[this] def read(
      uuid:        UUID,
      description: String,
      name:        String,
      hash:        String,
      created:     Option[Instant],
      owner:       UUID
  ): FileObject = {
    FileObject(
      uuid, description, name, hash, created, owner
    )
  }

  private[this] def write( request: FileObject ): ( UUID, String, String, String, Option[Instant], UUID ) = {
    ( request.uuid, request.description, request.name, request.hash, request.created, request.owner )
  }

}
