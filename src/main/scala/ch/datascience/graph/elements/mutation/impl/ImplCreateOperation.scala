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

package ch.datascience.graph.elements.mutation.impl

import ch.datascience.graph.elements.{Property, RichProperty}
import ch.datascience.graph.elements.mutation.CreateOperation
import ch.datascience.graph.elements.persistence.NewVertex
/**
  * Created by jeberle on 10.05.17.
  */
case class ImplCreateVertexOperation[TypeId, Key, +Value, MetaKey, +MetaValue, +MetaProp <: Property[MetaKey, MetaValue, MetaProp], +Prop <: RichProperty[Key, Value, MetaKey, MetaValue, MetaProp, Prop]](
  vertex: NewVertex[TypeId, Key, Value, MetaKey, MetaValue,MetaProp, Prop]
) extends CreateOperation[Nothing, Nothing, NewVertex[TypeId, Key, Value, MetaKey, MetaValue,MetaProp, Prop]]

/*
case class ImplCreateVertexPropertyOperation[+TypeId, Key, +Value, MetaKey, +MetaValue, +MetaProp <: Property[MetaKey, MetaValue, MetaProp], +Prop <: RichProperty[Key, Value, MetaKey, MetaValue, MetaProp, Prop]](
  parent: Vertex[TypeId, Key, Value, MetaKey, MetaValue, MetaProp, Prop]
  vertexProperty: NewVertexProperty[Key, Value, MetaKey, MetaValue,MetaProp, Prop]
) extends CreateOperation[TypeId, Vertex[TypeId, Key, Value, MetaKey, MetaValue, MetaProp, Prop], NewVertexProperty[TypeId, Key, Value, MetaKey, MetaValue, MetaProp, Prop]]

case class ImplCreateEdgeOperation[TypeId, Key, +Value, MetaKey, +MetaValue, +MetaProp <: Property[MetaKey, MetaValue, MetaProp], +Prop <: RichProperty[Key, Value, MetaKey, MetaValue, MetaProp, Prop]](
  edge: NewEdge[TypeId, Key, Value, MetaKey, MetaValue,MetaProp, Prop]
) extends CreateOperation[Nothing, Nothing, NewEdge[TypeId, Key, Value, MetaKey, MetaValue,MetaProp, Prop]]
*/