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

import scala.language.higherKinds

/**
  * Base trait for elements that hold multi-properties (single, set, or list cardinality)
  *
  * Properties can be validated (see package types).
  *
  */
trait HasMultiProperties[Key, Value, Prop[K, V] <: Property[K, V, Prop]] extends Element {

  implicit def validMultiPropertyValuesEvidence: ValidValue[Value]

  type MultiPropertiesType = MultiProperties[Key, Value, HasMultiPropertiesHelper[Key, Prop]#PropertyV]
  val properties: MultiPropertiesType

}

private[this] class HasMultiPropertiesHelper[Key, Prop[K, V] <: Property[K, V, Prop]] {
  type PropertyV[V] = Prop[Key, V]
}
