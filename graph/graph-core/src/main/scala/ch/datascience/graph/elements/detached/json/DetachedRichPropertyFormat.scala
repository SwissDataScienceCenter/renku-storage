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

package ch.datascience.graph.elements.detached.json

import ch.datascience.graph.elements.RichProperty
import ch.datascience.graph.elements.detached.{DetachedProperty, DetachedRichProperty}
import ch.datascience.graph.elements.json.{RichPropertyReads, RichPropertyWrites}
import play.api.libs.json._

/**
  * Created by johann on 31/05/17.
  */
object DetachedRichPropertyFormat extends Format[DetachedRichProperty] {

  def writes(prop: DetachedRichProperty): JsValue = writer.writes(prop)

  def reads(json: JsValue): JsResult[DetachedRichProperty] = for {
    prop <- reader.reads(json)
  } yield DetachedRichProperty(prop.key, prop.value, prop.properties)

  private[this] lazy val writer: Writes[RichProperty { type Prop = DetachedProperty }] = new RichPropertyWrites[DetachedProperty]()(DetachedPropertyFormat)

  private[this] lazy val reader: Reads[RichProperty { type Prop = DetachedProperty }] = new RichPropertyReads[DetachedProperty]()(DetachedPropertyFormat)

}
