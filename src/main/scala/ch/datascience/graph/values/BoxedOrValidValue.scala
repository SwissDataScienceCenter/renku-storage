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

package ch.datascience.graph.values

import ch.datascience.graph.types.DataType

/**
  * Trait for proving that a value of type V is either a [[BoxedValue]] or there is a proof that v is a valid value
  * (see: [[ValidValue]]).
  *
  * @tparam V analyzed type
  */
sealed trait BoxedOrValidValue[-V] {
  def isBoxed: Boolean

  def dataType[U <: V](v: U): DataType

  def castValue[U <: V](v: U): Either[BoxedValue, (U, ValidValue[U])]
}

object BoxedOrValidValue {

  implicit object BoxedValueEvidence extends BoxedOrValidValue[BoxedValue] {
    def isBoxed: Boolean = true

    def dataType[U <: BoxedValue](v: U): DataType = v.dataType

    def castValue[U <: BoxedValue](v: U): Either[BoxedValue, (U, ValidValue[U])] = Left(v)
  }

  implicit class ValidValueEvidence[V](evidence: ValidValue[V]) extends BoxedOrValidValue[V] {
    def isBoxed: Boolean = false

    def dataType[U <: V](v: U): DataType = evidence.dataType

    def castValue[U <: V](v: U): Either[BoxedValue, (U, ValidValue[U])] = Right((v, evidence))

    def validValue: ValidValue[V] = evidence
  }

  implicit def lift[V: ValidValue]: BoxedOrValidValue[V] = new ValidValueEvidence(implicitly[ValidValue[V]])

}
