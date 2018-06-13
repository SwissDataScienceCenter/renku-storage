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

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{ JsPath, OFormat }

case class LFSObjectResponse(
    oid:           String,
    size:          Long,
    authenticated: Boolean,
    actions:       Option[LFSDownload]
)

object LFSObjectResponse {

  implicit lazy val LFSDownloadFormat: OFormat[LFSDownload] = LFSDownload.format

  def format: OFormat[LFSObjectResponse] = (
    ( JsPath \ "oid" ).format[String] and
    ( JsPath \ "size" ).format[Long] and
    ( JsPath \ "authenticated" ).format[Boolean] and
    ( JsPath \ "actions" ).formatNullable[LFSDownload]
  )( read, write )

  private[this] def read(
      oid:           String,
      size:          Long,
      authenticated: Boolean,
      action:        Option[LFSDownload]
  ): LFSObjectResponse = {
    LFSObjectResponse(
      oid, size, authenticated, action
    )
  }

  private[this] def write( request: LFSObjectResponse ): ( String, Long, Boolean, Option[LFSDownload] ) = {
    ( request.oid, request.size, request.authenticated, request.actions )
  }

}
