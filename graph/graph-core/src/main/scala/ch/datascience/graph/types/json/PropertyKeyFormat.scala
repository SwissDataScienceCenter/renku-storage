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

import ch.datascience.graph.types.{DataType, PropertyKey, Cardinality}
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Created by johann on 19/06/17.
  */
object PropertyKeyFormat extends Format[PropertyKey] {

  def writes(propertyKey: PropertyKey): JsValue = writer.writes(propertyKey)

  def reads(json: JsValue): JsResult[PropertyKey] = reader.reads(json)

  private[this] def writer: Writes[PropertyKey] = (
    (JsPath \ "key").write[PropertyKey#Key](propKeyFormat) and
      (JsPath \ "data_type").write[DataType](DataTypeFormat) and
      (JsPath \ "cardinality").write[Cardinality](CardinalityFormat)
    )(unlift(PropertyKey.unapply))

  private[this] def reader: Reads[PropertyKey] = (
    (JsPath \ "key").read[PropertyKey#Key](propKeyFormat) and
      (JsPath \ "data_type").read[DataType](DataTypeFormat) and
      (JsPath \ "cardinality").read[Cardinality](CardinalityFormat)
    )(PropertyKey.apply _)
}
