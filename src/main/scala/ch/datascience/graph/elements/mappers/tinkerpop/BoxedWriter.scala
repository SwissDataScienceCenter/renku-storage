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

//package ch.datascience.graph.elements.mappers.tinkerpop
//
//import ch.datascience.graph.values._
//
///**
//  * Created by johann on 24/05/17.
//  */
//object BoxedWriter extends Writer[BoxedValue, java.lang.Object] {
//
//  def write(value: BoxedValue): java.lang.Object = value match {
//    case StringValue(self)  => self
//    case BooleanValue(self) => Boolean.box(self)
//    case ByteValue(self)    => Byte.box(self)
//    case CharValue(self)    => Char.box(self)
//    case DoubleValue(self)  => Double.box(self)
//    case FloatValue(self)   => Float.box(self)
//    case IntValue(self)     => Int.box(self)
//    case LongValue(self)    => Long.box(self)
//    case ShortValue(self)   => Short.box(self)
//    case UuidValue(self)    => self
//  }
//
//}
