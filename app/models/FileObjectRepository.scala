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

case class FileObjectRepository(
    fileObject: UUID,
    repository: UUID,
    iid:        Option[String],
    created:    Option[Instant]
)

object FileObjectRepository {

  def format: OFormat[FileObjectRepository] = (
    ( JsPath \ "object_uuid" ).format[UUID] and
    ( JsPath \ "repo_uuid" ).format[UUID] and
    ( JsPath \ "backend" ).formatNullable[String] and
    ( JsPath \ "created" ).formatNullable[Instant]
  )( read, write )

  private[this] def read(
      fileObject: UUID,
      repository: UUID,
      iid:        Option[String],
      created:    Option[Instant]
  ): FileObjectRepository = {
    FileObjectRepository(
      fileObject, repository, iid, created
    )
  }

  private[this] def write( request: FileObjectRepository ): ( UUID, UUID, Option[String], Option[Instant] ) = {
    ( request.fileObject, request.repository, request.iid, request.created )
  }

}
