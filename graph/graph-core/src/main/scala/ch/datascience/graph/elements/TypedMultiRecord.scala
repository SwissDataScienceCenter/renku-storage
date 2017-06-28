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

import ch.datascience.graph.Constants
import ch.datascience.graph.bases.HasTypes

trait TypedMultiRecord extends MultiRecord with HasTypes {

  final type TypeId = Constants.TypeId

}

///**
//  * Base trait for multi-records that have multi-properties which are constrained by types
//  *
//  * Typed  multi-records can be validated (see package types).
//  *
//  */
//trait TypedMultiRecord[TypeId, Key, +Value, +Prop <: Property[Key, Value]]
//  extends MultiRecord[Key, Value, Prop]
//    with HasTypes[TypeId]
