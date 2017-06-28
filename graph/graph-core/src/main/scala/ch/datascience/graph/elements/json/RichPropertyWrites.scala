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

import ch.datascience.graph.elements.{Property, Record, RichProperty}
import play.api.libs.json.{JsPath, JsValue, Writes}
import play.api.libs.functional.syntax._

/**
  * Created by johann on 24/05/17.
  */
class RichPropertyWrites[P <: Property : Writes] extends Writes[RichProperty { type Prop <: P }] {

  def writes(prop: RichProperty {type Prop <: P}): JsValue = self.writes(prop)

  private[this] lazy val self: Writes[RichProperty { type Prop <: P }] = (
    JsPath.write[Property](PropertyFormat) and
    JsPath.write[Record { type Prop <: P }](recordWrites)
  ) { prop => (prop, prop) }

  private[this] lazy val recordWrites = new RecordWrites[P]

}
