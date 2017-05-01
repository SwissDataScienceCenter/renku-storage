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

package ch.datascience.graph.elements.simple

import ch.datascience.graph.elements.{BoxedOrValidValue, HasValueMapper, Property}

/**
  * Created by johann on 27/04/17.
  */
final case class SimpleProperty[+Key, +Value : BoxedOrValidValue](key: Key, value: Value)
  extends Property[Key, Value, SimpleProperty[Key, Value]]

object SimpleProperty {

  class Mapper[Key, Value, V : BoxedOrValidValue] extends HasValueMapper[Value, SimpleProperty[Key, Value], V, SimpleProperty[Key, V]] {
    def map(sp: SimpleProperty[Key, Value])(f: (Value) => V): SimpleProperty[Key, V] = SimpleProperty(sp.key, f(sp.value))
  }

  lazy val reusableMapper: Mapper[Nothing, Nothing, Nothing] = new Mapper[Nothing, Nothing, Nothing]()

  implicit def canMap[Key, U, V : BoxedOrValidValue]: HasValueMapper[U, SimpleProperty[Key, U], V, SimpleProperty[Key, V]] = reusableMapper.asInstanceOf[Mapper[Key, U, V]]

}
