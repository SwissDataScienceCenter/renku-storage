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

package ch.datascience.graph.elements.new_

import ch.datascience.graph.elements.Vertex
import ch.datascience.graph.elements.detached.DetachedRichProperty
import ch.datascience.graph.elements.new_.impl.ImplNewVertex

/**
  * Created by johann on 29/05/17.
  */
trait NewVertex extends Vertex with NewElement {

  final type TempId = Int

  def tempId: TempId

  final type Prop = DetachedRichProperty

}

object NewVertex {

  def apply(
    tempId: NewVertex#TempId,
    types: Set[NewVertex#TypeId],
    properties: NewVertex#Properties
  ): NewVertex = ImplNewVertex(tempId, types, properties)

  def unapply(vertex: NewVertex): Option[(NewVertex#TempId, Set[NewVertex#TypeId], NewVertex#Properties)] = {
    if (vertex eq null)
      None
    else
      Some(vertex.tempId, vertex.types, vertex.properties)
  }

}
