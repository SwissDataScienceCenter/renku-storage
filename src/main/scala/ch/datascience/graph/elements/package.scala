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

package ch.datascience.graph

import language.higherKinds

/**
  * Created by johann on 28/04/17.
  */
package object elements {

  /**
    * Type used for properties
    *
    * @tparam Key key type
    * @tparam Value value type
    * @tparam Prop property type
    */
  type Properties[Key, +Value, +Prop <: Property[Key, Value, Prop]] = Map[Key, Property[Key, Value, Prop]]

  /**
    * Type used for multi-properties
    *
    * @tparam Key key type
    * @tparam Value value type
    * @tparam Prop property type
    */
  type MultiProperties[Key, +Value, +Prop <: Property[Key, Value, Prop]] = Map[Key, MultiPropertyValue[Key, Value, Property[Key, Value, Prop]]]

}
