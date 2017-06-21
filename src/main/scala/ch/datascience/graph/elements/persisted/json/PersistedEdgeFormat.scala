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

package ch.datascience.graph.elements.persisted.json

import ch.datascience.graph.elements.json.{EdgeReads, EdgeWrites, PropertyFormat}
import ch.datascience.graph.elements.persisted.{Path, PersistedEdge, PersistedRecordProperty, PersistedVertex}
import ch.datascience.graph.elements.{Edge, Property}
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by johann on 13/06/17.
  */
object PersistedEdgeFormat extends Format[PersistedEdge] {

  def writes(edge: PersistedEdge): JsValue = writer.writes(edge)

  def reads(json: JsValue): JsResult[PersistedEdge] = reader.reads(json)

  private[this] lazy val writer: Writes[PersistedEdge] = (
    (JsPath \ "id").write[PersistedEdge#Id] and
      JsPath.write[Edge { type VertexReference = VR; type Prop = PersistedRecordProperty }](edgeWriter)
  ) { edge => (edge.id, edge) }

  private[this] lazy val reader: Reads[PersistedEdge] = (
    (JsPath \ "id").read[PersistedEdge#Id] and
      JsPath.read[Edge { type VertexReference = VR; type Prop = PersistedRecordProperty }](edgeReader)
    ) { (id, edge) => PersistedEdge(id, edge.label, edge.from, edge.to, edge.properties) }

  private[this] type VR = PersistedVertex#Id

  private[this] lazy val edgeWriter: Writes[Edge { type VertexReference = VR; type Prop = PersistedRecordProperty }] = new EdgeWrites[VR, PersistedRecordProperty]()(implicitly[Writes[PersistedVertex#Id]], PersistedRecordPropertyFormat)

  private[this] lazy val edgeReader: Reads[Edge { type VertexReference = VR; type Prop = PersistedRecordProperty }] = new EdgeReads[VR, PersistedRecordProperty]()(implicitly[Reads[PersistedVertex#Id]], PersistedRecordPropertyFormat)

}
