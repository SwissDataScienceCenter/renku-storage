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

package ch.datascience
package graph
package elements

/**
  * Base trait for records, i.e. elements that hold properties
  */
trait Record extends Element {

  type Prop <: Property
  final type Properties = Map[Prop#Key, Prop]

  /**
    * Properties
    * @return the properties
    */
  def properties: Properties

//  protected[elements] def <|(graphType: GraphType { type Key = Prop#Key }): Boolean = graphType match {
//    case RecordType(props) => props subsetOf properties.keySet
//    case _ => false
//  }

}

///**
//  * Base trait for records, i.e. elements that hold properties
//  *
//  * Properties can be validated (see package types).
//  *
//  * @tparam Key   key type
//  * @tparam Value value type
//  * @tparam Prop  property type
//  */
//trait Record[Key, +Value, +Prop <: Property[Key, Value]] extends Element {
//
//  def properties: Properties[Key, Value, Prop]
//
//  protected[elements] def <|(graphType: GraphType): Boolean = graphType match {
//    case rt: RecordType[Key] => rt.properties subsetOf properties.keySet
//    case _ => false
//  }
//
//}
