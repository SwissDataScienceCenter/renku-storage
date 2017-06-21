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

import ch.datascience.graph.elements.{Edge, Property, Record}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsResult, JsValue, Reads}

/**
  * Created by johann on 08/06/17.
  */
class EdgeReads[V : Reads, P <: Property : Reads] extends Reads[Edge { type VertexReference = V; type Prop = P }] {

  def reads(json: JsValue): JsResult[Edge {type VertexReference = V; type Prop = P}] = self.reads(json)

  private[this] lazy val self: Reads[Edge { type VertexReference = V; type Prop = P }] = (
    (JsPath \ "label").read[Edge#Label] and
      (JsPath \ "from").read[V] and
      (JsPath \ "to").read[V] and
      JsPath.read[Record { type Prop = P }](recordReads)
  ) { (l, f, t, record) =>
    new Edge {
      type VertexReference = V
      type Prop = P
      def label: Label = l
      def from: V = f
      def to: V = t
      def properties: Properties = record.properties
    }
  }

  private[this] lazy val recordReads = new RecordReads[P]

  private[this] implicit lazy val labelReads: Reads[Edge#Label] = EdgeLabelFormat

}
