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

case class LFSBatchResponse(
    transfers: Option[Seq[String]],
    objects:   Seq[LFSObjectResponse]
)

object LFSBatchResponse {

  implicit lazy val LFSObjectResponseFormat: OFormat[LFSObjectResponse] = LFSObjectResponse.format

  def format: OFormat[LFSBatchResponse] = (
    ( JsPath \ "transfers" ).formatNullable[Seq[String]] and
    ( JsPath \ "objects" ).format[Seq[LFSObjectResponse]]
  )( { ( t, b ) => LFSBatchResponse( t, b ) }, { req => ( req.transfers, req.objects ) } )

}
