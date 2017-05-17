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

import ch.datascience.graph.elements.mutation.CreateOperation
/**
  * Created by jeberle on 10.05.17.
  */
case class ImplCreateVertexOperation[TypeId, Key, +Value, MetaKey](
  vertex: ImplNewVertex[TypeId, Key, Value, MetaKey]
) extends CreateOperation[ImplNewVertex[TypeId, Key, Value, MetaKey]]

case class ImplCreateEdgeOperation[+Id, Key, +Value](
 edge: ImplNewEdge[Id, Key, Value]
) extends CreateOperation[ImplNewEdge[Id, Key, Value]]


case class ImplCreateVertexPropertyOperation[+Key, +Value, MetaKey, +MetaValue](
  vertexProperty: ImplNewRecordRichProperty[Key, Value, MetaKey, MetaValue]
) extends CreateOperation[ImplNewRecordRichProperty[Key, Value, MetaKey, MetaValue]]

