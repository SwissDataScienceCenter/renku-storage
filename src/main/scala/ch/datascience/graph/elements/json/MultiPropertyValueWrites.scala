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

package ch.datascience.graph.elements.json

import ch.datascience.graph.elements.{MultiPropertyValue, Property}
import ch.datascience.graph.types.json.{CardinalityFormat, DataTypeFormat}
import ch.datascience.graph.types.{Cardinality, DataType}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsValue, Writes}

/**
  * Created by johann on 31/05/17.
  */
class MultiPropertyValueWrites[P <: Property : Writes] extends Writes[MultiPropertyValue[P]] {

  def writes(value: MultiPropertyValue[P]): JsValue = self.writes(value)

  private[this] lazy val self: Writes[MultiPropertyValue[P]] = (
    (JsPath \ "key").write[P#Key](KeyFormat) and
      (JsPath \ "data_type").write[DataType](DataTypeFormat) and
      (JsPath \ "cardinality").write[Cardinality](CardinalityFormat) and
      (JsPath \ "values").write[Iterable[P]]
  ) { value => (value.key, value.dataType, value.cardinality, value.asIterable) }

}
