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

import ch.datascience.graph.types.Cardinality
import language.higherKinds

/**
  * Created by johann on 27/04/17.
  */
sealed abstract class VertexPropertyValues[
    Key,
    Value : ValidValue,
    MetaKey,
    MetaProp[K, V] <: Property[K, V, MetaProp],
    VertexPropertyType[K, V, MK, MP[MPK, MPV] <: Property[MPK, MPV, MP]] <: VertexProperty[K, V, MK, MP, VertexPropertyType]
] extends Element with Iterable[VertexPropertyType[Key, Value, MetaKey, MetaProp]] {

  // Allows easy iteration
  final def asSeq: Seq[VertexPropertyType[Key, Value, MetaKey, MetaProp]] = this match {
    case SingleValue(vp) => List(vp)
    case SetValue(map) => map.values.toSeq
    case ListValue(vps) => vps
  }

  override final def iterator: Iterator[VertexPropertyType[Key, Value, MetaKey, MetaProp]] = asSeq.toIterator

  final def boxed: VertexPropertyValues[Key, BoxedValue, MetaKey, MetaProp, VertexPropertyType] = this match {
    case SingleValue(vp) => SingleValue[Key, BoxedValue, MetaKey, MetaProp, VertexPropertyType](vp.boxed)
    case SetValue(map) => SetValue[Key, BoxedValue, MetaKey, MetaProp, VertexPropertyType]((for (vp <- map.values) yield vp.boxedValue -> vp.boxed).toMap)
    case ListValue(vps) => ListValue[Key, BoxedValue, MetaKey, MetaProp, VertexPropertyType](vps.map(_.boxed))
  }

  final def cardinality: Cardinality = this match {
    case SingleValue(_) => Cardinality.Single
    case SetValue(_) => Cardinality.Set
    case ListValue(_) => Cardinality.List
  }

}

final case class SingleValue[
    Key,
    Value : ValidValue,
    MetaKey,
    MetaProp[K, V] <: Property[K, V, MetaProp],
    VertexPropertyType[K, V, MK, MP[MPK, MPV] <: Property[MPK, MPV, MP]] <: VertexProperty[K, V, MK, MP, VertexPropertyType]
](
    vertexProperty: VertexPropertyType[Key, Value, MetaKey, MetaProp]
) extends VertexPropertyValues[Key, Value, MetaKey, MetaProp, VertexPropertyType]

final case class SetValue[
    Key,
    Value : ValidValue,
    MetaKey,
    MetaProp[K, V] <: Property[K, V, MetaProp],
    VertexPropertyType[K, V, MK, MP[MPK, MPV] <: Property[MPK, MPV, MP]] <: VertexProperty[K, V, MK, MP, VertexPropertyType]
](
    vertexProperties: Map[Value, VertexPropertyType[Key, Value, MetaKey, MetaProp]]
 ) extends VertexPropertyValues[Key, Value, MetaKey, MetaProp, VertexPropertyType]

final case class ListValue[
    Key,
    Value : ValidValue,
    MetaKey,
    MetaProp[K, V] <: Property[K, V, MetaProp],
    VertexPropertyType[K, V, MK, MP[MPK, MPV] <: Property[MPK, MPV, MP]] <: VertexProperty[K, V, MK, MP, VertexPropertyType]
](
    vertexProperties: List[VertexPropertyType[Key, Value, MetaKey, MetaProp]]
 ) extends VertexPropertyValues[Key, Value, MetaKey, MetaProp, VertexPropertyType]

object SetValue {

  def apply[
      Key,
      Value : ValidValue,
      MetaKey,
      MetaProp[K, V] <: Property[K, V, MetaProp],
      VertexPropertyType[K, V, MK, MP[MPK, MPV] <: Property[MPK, MPV, MP]] <: VertexProperty[K, V, MK, MP, VertexPropertyType]
  ](vertexProperties: Iterable[VertexPropertyType[Key, Value, MetaKey, MetaProp]]): SetValue[Key, Value, MetaKey, MetaProp, VertexPropertyType] = {
    val map = vertexProperties.map(vp => vp.value -> vp).toMap
    val keys = map.values.map(_.key).toSet
    if(keys.size > 1)
      throw new IllegalArgumentException(s"Multiple keys: ${keys.mkString(", ")}")
    SetValue[Key, Value, MetaKey, MetaProp, VertexPropertyType](map)
  }

}
