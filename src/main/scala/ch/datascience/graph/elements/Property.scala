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

package ch.datascience.graph.elements

import ch.datascience.graph.types.DataType
import language.higherKinds

/**
  * Created by johann on 27/04/17.
  */
abstract class Property[Key, Value : ValidValue, This[K, V] <: Property[K, V, This]] extends Element with HasValue[Value, PropertyHelper[Key, This]#PropertyV] {

  val key: Key

  val value: Value

  override final def validValueEvidence: ValidValue[Value] = implicitly[ValidValue[Value]]

}

private[this] class PropertyHelper[Key, This[K, V] <: Property[K, V, This]] {
  type PropertyV[V] = This[Key, V]
}
