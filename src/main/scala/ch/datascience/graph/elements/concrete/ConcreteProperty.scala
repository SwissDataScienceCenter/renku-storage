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

package ch.datascience.graph.elements.concrete

import ch.datascience.graph.elements.{BoxedOrValidValue, HasPath, HasValueMapper, Property}

/**
  * Created by julien on 10/05/17.
  */
final case class ConcreteProperty[+Path, +Key, +Value: BoxedOrValidValue](path: Path, key: Key, value: Value)
  extends Property[Key, Value, ConcreteProperty[Path, Key, Value]]
  with HasPath[Path]

object ConcreteProperty {

  lazy val reusableMapper: Mapper[Nothing, Nothing, Nothing, Nothing] = new Mapper[Nothing, Nothing, Nothing, Nothing]()

  class Mapper[Path, Key, U, V: BoxedOrValidValue] extends HasValueMapper[U, ConcreteProperty[Path, Key, U], V,
    ConcreteProperty[Path, Key, V]] {
    def map(sp: ConcreteProperty[Path, Key, U])(f: (U) => V): ConcreteProperty[Path, Key, V] = ConcreteProperty(sp.path, sp.key, f(sp.value))
  }

  implicit def canMap[Path, Key, U, V: BoxedOrValidValue]: HasValueMapper[U, ConcreteProperty[Path, Key, U], V, ConcreteProperty[Path, Key, V]] =
    reusableMapper.asInstanceOf[Mapper[Path, Key, U, V]]

}
