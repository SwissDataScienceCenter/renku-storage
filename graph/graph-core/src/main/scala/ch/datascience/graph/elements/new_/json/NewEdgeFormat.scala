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

package ch.datascience.graph.elements.new_.json

import ch.datascience.graph.elements.Edge
import ch.datascience.graph.elements.detached.DetachedProperty
import ch.datascience.graph.elements.detached.json.DetachedPropertyFormat
import ch.datascience.graph.elements.json.{EdgeReads, EdgeWrites}
import ch.datascience.graph.elements.new_.NewEdge
import play.api.libs.json._

/**
  * Created by johann on 08/06/17.
  */
object NewEdgeFormat extends Format[NewEdge] {

  def writes(edge: NewEdge): JsValue = edgeWriter.writes(edge)

  def reads(json: JsValue): JsResult[NewEdge] = edgeReader.reads(json).map { edge =>
    NewEdge(edge.label, edge.from, edge.to, edge.properties)
  }

  private[this] type VR = Either[NewEdge#NewVertexType#TempId, NewEdge#PersistedVertexType#Id]

  private[this] lazy val edgeWriter: Writes[Edge { type VertexReference = VR; type Prop = DetachedProperty }] = new EdgeWrites[VR, DetachedProperty]()(VertexReferenceWrites, DetachedPropertyFormat)

  private[this] lazy val edgeReader: Reads[Edge { type VertexReference = VR; type Prop = DetachedProperty }] = new EdgeReads[VR, DetachedProperty]()(VertexReferenceReads, DetachedPropertyFormat)


}
