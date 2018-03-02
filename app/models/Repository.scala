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

case class Repository(
    uuid:        UUID,
    iid:         Option[String],
    description: String,
    path:        String,
    backend:     String,
    created:     Option[Instant],
    owner:       Option[UUID],
    lfs_store:   Option[UUID]
)

object Repository {

  def format: OFormat[Repository] = (
    ( JsPath \ "uuid" ).format[UUID] and
    ( JsPath \ "iid" ).formatNullable[String] and
    ( JsPath \ "description" ).format[String] and
    ( JsPath \ "path" ).format[String] and
    ( JsPath \ "backend" \ "name" ).format[String] and
    ( JsPath \ "created" ).formatNullable[Instant] and
    ( JsPath \ "owner" ).formatNullable[UUID] and
    ( JsPath \ "lfs_store" ).formatNullable[UUID]
  )( read, write )

  private[this] def read(
      uuid:        UUID,
      iid:         Option[String],
      description: String,
      path:        String,
      backend:     String,
      created:     Option[Instant],
      owner:       Option[UUID],
      lfs_store:   Option[UUID]
  ): Repository = {
    Repository(
      uuid, iid, description, path, backend, created, owner, lfs_store
    )
  }

  private[this] def write( request: Repository ): ( UUID, Option[String], String, String, String, Option[Instant], Option[UUID], Option[UUID] ) = {
    ( request.uuid, request.iid, request.description, request.path, request.backend, request.created, request.owner, request.lfs_store )
  }

}
