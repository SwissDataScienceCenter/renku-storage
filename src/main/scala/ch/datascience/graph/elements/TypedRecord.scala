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

import ch.datascience.graph.bases.HasTypes
import ch.datascience.graph.Constants

trait TypedRecord extends Record with HasTypes {

  final type TypeId = Constants.TypeId

  //  protected[elements] def <|(graphType: GraphType { type TypeId = self.TypeId; type Key = Prop#Key }): Boolean = graphType match {
  //    case NamedType(tid, _, _) => types contains tid
  //    case _                    => super.<|(graphType)
  //  }
}

///**
//  * Base trait for records that have properties which are constrained by types
//  *
//  * Typed records can be validated (see package types).
//  *
//  */
//trait TypedRecord[TypeId, Key, +Value, +Prop <: Property[Key, Value]]
//  extends Record[Key, Value, Prop]
//    with HasTypes[TypeId] {
//
//  protected[elements] override def <|(graphType: GraphType): Boolean = graphType match {
//    case nrt: NamedType[TypeId, Key] => types contains nrt.key
//    case _ => super.<|(graphType)
//  }
//
//}
