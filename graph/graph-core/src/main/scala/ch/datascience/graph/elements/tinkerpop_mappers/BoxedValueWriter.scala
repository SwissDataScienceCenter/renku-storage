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

package ch.datascience.graph.elements.tinkerpop_mappers

import ch.datascience.graph.values._

/**
  * Created by johann on 30/05/17.
  */
case object BoxedValueWriter extends Writer[BoxedValue, java.lang.Object] {

  def write(value: BoxedValue): java.lang.Object = value match {
    case StringValue(str) => str
    case CharValue(x) => Char.box(x)
    case BooleanValue (x) => Boolean.box(x)
    case ByteValue    (x) => Byte.box(x)
    case ShortValue   (x) => Short.box(x)
    case IntValue     (x) => Int.box(x)
    case LongValue    (x) => Long.box(x)
    case FloatValue   (x) => Float.box(x)
    case DoubleValue  (x) => Double.box(x)
    case UuidValue    (uuid) => uuid
  }

}
