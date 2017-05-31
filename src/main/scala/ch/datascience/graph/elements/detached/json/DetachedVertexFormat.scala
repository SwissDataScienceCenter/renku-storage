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

import ch.datascience.graph.elements.{RichProperty, Vertex}
import ch.datascience.graph.elements.detached.{DetachedProperty, DetachedRichProperty, DetachedVertex}
import ch.datascience.graph.elements.json.{RichPropertyReads, RichPropertyWrites, VertexReads, VertexWrites}
import play.api.libs.json._

/**
  * Created by johann on 31/05/17.
  */
object DetachedVertexFormat extends Format[DetachedVertex] {

  def writes(vertex: DetachedVertex): JsValue = writer.writes(vertex)

  def reads(json: JsValue): JsResult[DetachedVertex] = for {
    vertex <- reader.reads(json)
  } yield DetachedVertex(vertex.types, vertex.properties)

  private[this] lazy val writer: Writes[Vertex { type Prop = DetachedRichProperty }] = new VertexWrites[DetachedRichProperty]()(DetachedRichPropertyFormat)

  private[this] lazy val reader: Reads[Vertex { type Prop = DetachedRichProperty }] = new VertexReads[DetachedRichProperty]()(DetachedRichPropertyFormat)

}
