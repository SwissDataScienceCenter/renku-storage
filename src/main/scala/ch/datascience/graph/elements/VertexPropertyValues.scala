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

/**
  * Created by johann on 27/04/17.
  */
sealed abstract class VertexPropertyValues[Key, Value : ValidValue, MetaKey] extends Element with Iterable[VertexProperty[Key, Value, MetaKey]] {

  // Allows easy iteration
  final def asSeq: Seq[VertexProperty[Key, Value, MetaKey]] = this match {
    case SingleValue(vp) => List(vp)
    case SetValue(map) => map.values.toSeq
    case ListValue(vps) => vps
  }

  override final def iterator: Iterator[VertexProperty[Key, Value, MetaKey]] = asSeq.toIterator

  final def boxed: VertexPropertyValues[Key, BoxedValue, MetaKey] = this match {
    case SingleValue(vp) => SingleValue(vp.boxed)
    case SetValue(map) => SetValue((for (vp <- map.values) yield vp.boxedValue -> vp.boxed).toMap)
    case ListValue(vps) => ListValue(vps.map(_.boxed))
  }

}

final case class SingleValue[Key, Value : ValidValue, MetaKey](vertexProperty: VertexProperty[Key, Value, MetaKey]) extends VertexPropertyValues[Key, Value, MetaKey]

final case class SetValue[Key, Value : ValidValue, MetaKey](vertexProperties: Map[Value, VertexProperty[Key, Value, MetaKey]]) extends VertexPropertyValues[Key, Value, MetaKey]

final case class ListValue[Key, Value : ValidValue, MetaKey](vertexProperties: Seq[VertexProperty[Key, Value, MetaKey]]) extends VertexPropertyValues[Key, Value, MetaKey]

object SetValue {

  def apply[Key, Value : ValidValue, MetaKey](vertexProperties: Iterable[VertexProperty[Key, Value, MetaKey]]): SetValue[Key, Value, MetaKey] = {
    val map = vertexProperties.map(vp => vp.value -> vp).toMap
    val keys = map.values.map(_.key).toSet
    if(keys.size > 1)
      throw new IllegalArgumentException(s"Multiple keys: ${keys.mkString(", ")}")
    SetValue(map)
  }

}
