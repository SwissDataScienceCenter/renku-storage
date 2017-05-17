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

import ch.datascience.graph.elements.MultiProperties
import ch.datascience.graph.elements.persistence.{NewEdge, NewVertex}

/**
  * Created by jeberle on 15.05.17.
  */
case class ImplNewEdge[
  +Id,
  Key,
  +Value
](
   tempId: NewEdge[Nothing, Nothing, Nothing, Nothing]#TempId,
   from: Either[Id, NewVertex[Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing]#TempId],
   to: Either[Id, NewVertex[Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing]#TempId],
   properties: MultiProperties[Key, Value, ImplNewMultiRecordProperty[Key, Value]]
 ) extends NewEdge[Id, Key, Value, ImplNewMultiRecordProperty[Key, Value]]
