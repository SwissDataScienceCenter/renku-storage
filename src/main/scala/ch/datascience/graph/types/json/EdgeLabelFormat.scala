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

package ch.datascience.graph.types.json

import ch.datascience.graph.types.{Multiplicity, EdgeLabel}
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by johann on 19/06/17.
  */
object EdgeLabelFormat extends Format[EdgeLabel] {

  def writes(edgeLabel: EdgeLabel): JsValue = writer.writes(edgeLabel)

  def reads(json: JsValue): JsResult[EdgeLabel] = reader.reads(json)

  private[this] def writer: Writes[EdgeLabel] = (
    (JsPath \ "key").write[EdgeLabel#Key](propKeyFormat) and
      (JsPath \ "multiplicity").write[Multiplicity](MultiplicityFormat)
    )(unlift(EdgeLabel.unapply))

  private[this] def reader: Reads[EdgeLabel] = (
    (JsPath \ "key").read[EdgeLabel#Key](propKeyFormat) and
      (JsPath \ "multiplicity").read[Multiplicity](MultiplicityFormat)
    )(EdgeLabel.apply _)
}
