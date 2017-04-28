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
trait HasValue[Value, This[V] <: HasValue[V, This]] {
  val value: Value

  implicit def validValueEvidence: ValidValue[Value]

  def dataType: DataType = implicitly[ValidValue[Value]].dataType(value)

  def boxed: This[BoxedValue] = this map { _ => boxedValue }

  def unboxed: This[_] = value match {
    case b: BoxedValue => this.map({ _ => b.self })(b.isValidValue)
    case _ => this map{ x => x }
  }

  @throws[java.lang.ClassCastException]
  def unboxedAs[V : ValidValue]: This[V] = {
    // Force cast
    implicitly[ValidValue[V]].dataType(unboxedValue.asInstanceOf[V])
    this map {
      case b: BoxedValue => b.unboxAs[V]
      case _ => value.asInstanceOf[V]
    }
  }

  def boxedValue: BoxedValue = implicitly[ValidValue[Value]].boxed(value)

  def unboxedValue: Any = value match {
    case b: BoxedValue => b.self
    case _ => value
  }

  @throws[java.lang.ClassCastException]
  def unboxedValueAs[V : ValidValue]: V = unboxedValue.asInstanceOf[V]

  def map[U : ValidValue](f: (Value) => U): This[U]

}
