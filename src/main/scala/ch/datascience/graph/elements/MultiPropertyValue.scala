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
sealed abstract class MultiPropertyValue[Value : ValidValue, Holder[V] <: HasValue[V, Holder]]
  extends Element with Iterable[Holder[Value]] {

  // Allows easy iteration
  final def asSeq: Seq[Holder[Value]] = this match {
    case SingleValue(holder) => List(holder)
    case SetValue(map) => map.values.toSeq
    case ListValue(values) => values
  }

  override final def iterator: Iterator[Holder[Value]] = asSeq.toIterator

  final def boxed: MultiPropertyValue[BoxedValue, Holder] = this match {
    case SingleValue(holder) => SingleValue(holder.boxed)
      case SetValue(map) => SetValue((for { h <- map.values } yield h.boxedValue -> h.boxed).toMap)
      case ListValue(values) => ListValue(for { h <- values } yield h.boxed)
  }

  final def cardinality: Cardinality = this match {
    case SingleValue(_) => Cardinality.Single
    case SetValue(_) => Cardinality.Set
    case ListValue(_) => Cardinality.List
  }

}

sealed abstract class SingleValue[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](val holder: Holder[Value])
  extends MultiPropertyValue[Value, Holder] {
  override final def toString(): String = super.toString()
}

sealed abstract class SetValue[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](val values: Map[Value, Holder[Value]])
  extends MultiPropertyValue[Value, Holder] {
  override final def toString(): String = super.toString()
}

sealed abstract class ListValue[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](val values: List[Holder[Value]])
  extends MultiPropertyValue[Value, Holder] {
  override final def toString(): String = super.toString()
}

object SingleValue {

  def apply[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](holder: Holder[Value]): SingleValue[Value, Holder] = SingleValueImpl(holder)

  def unapply[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](arg: MultiPropertyValue[Value, Holder]): Option[Holder[Value]] = arg match {
    case SingleValueImpl(holder) => Some(holder)
    case _ => None
  }

  private[this] case class SingleValueImpl[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](override val holder: Holder[Value])
    extends SingleValue[Value, Holder](holder)

}

object SetValue {

  def apply[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](values: (Value, Holder[Value])*): SetValue[Value, Holder] = SetValue(values.toMap)

  def apply[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](values: Map[Value, Holder[Value]]): SetValue[Value, Holder] = SetValueImpl(values)
  def unapply[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](arg: MultiPropertyValue[Value, Holder]): Option[Map[Value, Holder[Value]]] = arg match {
    case SetValueImpl(values) => Some(values)
    case _ => None
  }

  private[this] case class SetValueImpl[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](override val values: Map[Value, Holder[Value]])
    extends SetValue[Value, Holder](values)

}

object ListValue {

  def apply[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](values: (Holder[Value])*): ListValue[Value, Holder] = ListValue(values.toList)

  def apply[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](values: List[Holder[Value]]): ListValue[Value, Holder] = ListValueImpl(values)
  def unapply[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](arg: MultiPropertyValue[Value, Holder]): Option[List[Holder[Value]]] = arg match {
    case ListValueImpl(values) => Some(values)
    case _ => None
  }

  private[this] case class ListValueImpl[Value : ValidValue, Holder[V] <: HasValue[V, Holder]](override val values: List[Holder[Value]])
    extends ListValue[Value, Holder](values)

}
