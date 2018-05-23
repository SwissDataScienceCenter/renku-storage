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
import play.api.libs.json.{ JsPath, OFormat }

case class LFSDownload(
    href:       String,
    header:     Map[String, String],
    expiration: Long
)

object LFSDownload {

  def format: OFormat[LFSDownload] = (
    ( JsPath \ "download" \ "href" ).format[String] and
    ( JsPath \ "download" \ "header" ).format[Map[String, String]] and
    ( JsPath \ "download" \ "expires_in" ).format[Long]
  )( read, write )

  private[this] def read(
      href:       String,
      header:     Map[String, String],
      expiration: Long
  ): LFSDownload = {
    LFSDownload(
      href, header, expiration
    )
  }

  private[this] def write( request: LFSDownload ): ( String, Map[String, String], Long ) = {
    ( request.href, request.header, request.expiration )
  }

}
