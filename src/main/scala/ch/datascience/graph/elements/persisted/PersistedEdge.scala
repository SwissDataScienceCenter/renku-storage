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

package ch.datascience.graph.elements.persisted

import ch.datascience.graph.Constants
import ch.datascience.graph.bases.HasId
import ch.datascience.graph.elements.Edge
import ch.datascience.graph.elements.persisted.impl.ImplPersistedEdge

/**
  * Created by johann on 29/05/17.
  */
trait PersistedEdge
  extends Edge
    with PersistedElement
    with HasId {

  final type Id = Constants.EdgeId

  final type VertexReference = PersistedVertex#Id

  final type PathType = EdgePath

  final type Prop = PersistedRecordProperty

  final def path: EdgePath = EdgePath(id)

}

object PersistedEdge {

  def apply(
    id: PersistedEdge#Id,
    label: PersistedEdge#Label,
    from: PersistedEdge#VertexReference,
    to: PersistedEdge#VertexReference,
    properties: PersistedEdge#Properties
  ): PersistedEdge = ImplPersistedEdge(id, label, from, to, properties)

  def unapply(edge: PersistedEdge): Option[(PersistedEdge#Id, PersistedEdge#Label, PersistedEdge#VertexReference, PersistedEdge#VertexReference, PersistedEdge#Properties)] = {
    if (edge eq null)
      None
    else
      Some(edge.id, edge.label, edge.from, edge.to, edge.properties)
  }

}
