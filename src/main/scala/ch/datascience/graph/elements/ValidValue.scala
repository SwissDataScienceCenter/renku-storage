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

/**
  * Created by johann on 27/04/17.
  */
sealed trait ValidValue[V] {

  def dataType(value: V): DataType

  def boxed(value: V): BoxedValue

}

object ValidValue {

  // Valid value types
  implicit object StringIsValid extends ValidValue[String] {
    def dataType(value: String) = DataType.String
    def boxed(value: String) = BoxedValue(value)
  }

  implicit object CharIsValid extends ValidValue[Char] {
    def dataType(value: Char) = DataType.Character
    def boxed(value: Char) = BoxedValue(value)
  }

  implicit object BooleanIsValid extends ValidValue[Boolean] {
    def dataType(value: Boolean) = DataType.Boolean
    def boxed(value: Boolean) = BoxedValue(value)
  }

  implicit object ByteIsValid extends ValidValue[Byte] {
    def dataType(value: Byte) = DataType.Byte
    def boxed(value: Byte) = BoxedValue(value)
  }

  implicit object ShortIsValid extends ValidValue[Short] {
    def dataType(value: Short) = DataType.Short
    def boxed(value: Short) = BoxedValue(value)
  }

  implicit object IntIsValid extends ValidValue[Int] {
    def dataType(value: Int) = DataType.Integer
    def boxed(value: Int) = BoxedValue(value)
  }

  implicit object LongIsValid extends ValidValue[Long] {
    def dataType(value: Long) = DataType.Long
    def boxed(value: Long) = BoxedValue(value)
  }

  implicit object FloatIsValid extends ValidValue[Float] {
    def dataType(value: Float) = DataType.Float
    def boxed(value: Float) = BoxedValue(value)
  }

  implicit object DoubleIsValid extends ValidValue[Double] {
    def dataType(value: Double) = DataType.Double
    def boxed(value: Double) = BoxedValue(value)
  }

  implicit object BoxedIsValid extends ValidValue[BoxedValue] {
    def dataType(value: BoxedValue): DataType = value.dataType
    def boxed(value: BoxedValue) = value
  }

}
