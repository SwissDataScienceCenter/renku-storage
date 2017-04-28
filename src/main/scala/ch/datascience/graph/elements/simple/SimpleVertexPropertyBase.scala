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

import ch.datascience.graph.elements._

import language.higherKinds

/**
  * Created by johann on 27/04/17.
  */
final case class SimpleVertexProperty[Key, Value : ValidValue, MetaKey](
    override val key: Key,
    override val value: Value,
    override val metaProperties: Map[MetaKey, SimpleProperty[MetaKey, BoxedValue]]
) extends SimpleVertexPropertyBase[Key, Value, MetaKey, SimpleProperty](key, value, metaProperties)

class SimpleVertexPropertyBase[Key, Value : ValidValue, MetaKey, MetaProp[K, V] <: Property[K, V, MetaProp]](
    override val key: Key,
    override val value: Value,
    override val metaProperties: Map[MetaKey, MetaProp[MetaKey, BoxedValue]]
) extends VertexProperty[Key, Value, MetaKey, MetaProp, SimpleVertexPropertyBase] {

  override def map[U: ValidValue](f: (Value) => U) = new SimpleVertexPropertyBase(key, f(value), metaProperties)

}


object SimpleVertexProperty {

  def apply[Key, Value : ValidValue, MetaKey](key: Key, value: Value): SimpleVertexProperty[Key, Value, MetaKey] = {
    SimpleVertexProperty(key, value, Map.empty)
  }

}
