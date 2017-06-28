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

import java.util.UUID

import ch.datascience.graph.types.DataType

/**
  * Created by johann on 27/04/17.
  */
sealed trait ValidValue[-V] {

  def dataType: DataType

  def boxed[U <: V](value: U): BoxedValue

}

object ValidValue {

  // Valid value types
  implicit object StringIsValid extends ValidValue[String] {
    def dataType = DataType.String

    def boxed[U <: String](value: U) = BoxedValue(value)
  }

  implicit object CharIsValid extends ValidValue[Char] {
    def dataType = DataType.Character

    def boxed[U <: Char](value: U): BoxedValue = BoxedValue(value)
  }

  implicit object BooleanIsValid extends ValidValue[Boolean] {
    def dataType = DataType.Boolean

    def boxed[U <: Boolean](value: U): BoxedValue = BoxedValue(value)
  }

  implicit object ByteIsValid extends ValidValue[Byte] {
    def dataType = DataType.Byte

    def boxed[U <: Byte](value: U): BoxedValue = BoxedValue(value)
  }

  implicit object ShortIsValid extends ValidValue[Short] {
    def dataType = DataType.Short

    def boxed[U <: Short](value: U): BoxedValue = BoxedValue(value)
  }

  implicit object IntIsValid extends ValidValue[Int] {
    def dataType = DataType.Integer

    def boxed[U <: Int](value: U): BoxedValue = BoxedValue(value)
  }

  implicit object LongIsValid extends ValidValue[Long] {
    def dataType = DataType.Long

    def boxed[U <: Long](value: U): BoxedValue = BoxedValue(value)
  }

  implicit object FloatIsValid extends ValidValue[Float] {
    def dataType = DataType.Float

    def boxed[U <: Float](value: U): BoxedValue = BoxedValue(value)
  }

  implicit object DoubleIsValid extends ValidValue[Double] {
    def dataType = DataType.Double

    def boxed[U <: Double](value: U): BoxedValue = BoxedValue(value)
  }

  implicit object UuidIsValid extends ValidValue[UUID] {
    def dataType = DataType.UUID

    def boxed[U <: UUID](value: U): BoxedValue = BoxedValue(value)
  }

  implicit object NothingIsValid extends ValidValue[Nothing] {
    def dataType = DataType.Double

    def boxed[U <: Double](value: U): BoxedValue = BoxedValue(value)
  }

}
