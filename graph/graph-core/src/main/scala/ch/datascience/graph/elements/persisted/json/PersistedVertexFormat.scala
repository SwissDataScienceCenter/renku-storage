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

import ch.datascience.graph.elements.Vertex
import ch.datascience.graph.elements.detached.DetachedRichProperty
import ch.datascience.graph.elements.detached.json.DetachedRichPropertyFormat
import ch.datascience.graph.elements.json.{VertexReads, VertexWrites}
import ch.datascience.graph.elements.new_.NewVertex
import ch.datascience.graph.elements.persisted.{PersistedMultiRecordRichProperty, PersistedVertex, PersistedVertexProperty}
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by johann on 31/05/17.
  */

object PersistedVertexFormat extends Format[PersistedVertex] {

  def writes(vertex: PersistedVertex): JsValue = writer.writes(vertex)

  def reads(json: JsValue): JsResult[PersistedVertex] = reader.reads(json)

  private[this] lazy val writer: Writes[PersistedVertex] = (
    (JsPath \ "id").write[PersistedVertex#Id] and
      JsPath.write[Vertex { type Prop = PersistedVertexProperty }](vertexWriter)
  ) { vertex => (vertex.id, vertex) }

  private[this] lazy val reader: Reads[PersistedVertex] = (
    (JsPath \ "id").read[PersistedVertex#Id] and
      JsPath.read[Vertex { type Prop = PersistedVertexProperty }](vertexReader)
  ).apply { (id, vertex) => PersistedVertex(id, vertex.types, vertex.properties) }

  private[this] lazy val vertexWriter: Writes[Vertex { type Prop = PersistedVertexProperty }] = new VertexWrites[PersistedVertexProperty]()(PersistedVertexPropertyFormat)

  private[this] lazy val vertexReader: Reads[Vertex { type Prop = PersistedVertexProperty }] = new VertexReads[PersistedVertexProperty]()(PersistedVertexPropertyFormat)

}
