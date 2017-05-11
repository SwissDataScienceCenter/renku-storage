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

package ch.datascience.graph.elements.persistence

import ch.datascience.graph.elements._

/**
  * Created by johann on 11/05/17.
  */
sealed trait NewOrPersistedElement extends Element

sealed trait PersistedElement[+P <: Path] extends NewOrPersistedElement with HasPath[P]

sealed trait NewElement extends NewOrPersistedElement

trait PersistedVertex[
+Id,
TypeId,
Key,
+Value,
MetaKey,
+MetaValue,
+MetaProp <: PersistedRecordProperty[MetaKey, MetaValue, MetaProp],
+PropId,
+Prop <: RichProperty[Key, Value, MetaKey, MetaValue, MetaProp, Prop] with PersistedMultiRecordProperty[PropId, Key, Value, Prop] with HasPath[RecordPath[MetaKey]]
] extends Vertex[TypeId, Key, Value, MetaKey, MetaValue, MetaProp, Prop]
  with PersistedElement[VertexPath[Id] with MultiRecordPath[PropId]]
  with HasId[Id] {

  final def path: VertexPath[Id] with MultiRecordPath[PropId] = new VertexPath(id) with MultiRecordPath[PropId]

}

sealed trait PersistedProperty[+Key, +Value, +This <: PropertyBase[Key, Value]]
  extends Property[Key, Value, This]
    with PersistedElement[PropertyPath] { this: This =>
}

trait PersistedRecordProperty[+Key, +Value, +This <: PropertyBase[Key, Value]]
  extends PersistedProperty[Key, Value, This]
    with PersistedElement[PropertyPathWithKey[Key]] { this: This =>

  def parent: RecordPath[Key]

  def path: PropertyPathWithKey[Key] = PropertyPathWithKey(parent, key)

}

trait PersistedMultiRecordProperty[+Id, +Key, +Value, +This <: PropertyBase[Key, Value]]
  extends PersistedProperty[Key, Value, This]
    with PersistedElement[PropertyPathWithId[Id]]
    with HasId[Id] { this: This =>

  def parent: MultiRecordPath[Id]

  def path: PropertyPathWithId[Id] = PropertyPathWithId(parent, id)

}

trait PersistedRecordRichProperty[+Key, +Value, MetaKey, +MetaValue, +MetaProp <: PersistedRecordProperty[MetaKey, MetaValue, MetaProp],
+This <: RichPropertyBase[Key, Value, MetaKey, MetaValue, MetaProp]]
  extends PersistedProperty[Key, Value, This]
    with PersistedRecordProperty[Key, Value, This]
    with RichProperty[Key, Value, MetaKey, MetaValue, MetaProp, This]
    with PersistedElement[PropertyPathWithKey[Key] with RecordPath[MetaKey]] { this: This =>

  def parent: RecordPath[Key]

  override final def path: PropertyPathWithKey[Key] with RecordPath[MetaKey] = new PropertyPathWithKey(parent, key) with RecordPath[MetaKey]

}

trait PersistedMultiRecordRichProperty[+Id, +Key, +Value, MetaKey, +MetaValue, +MetaProp <: PersistedRecordProperty[MetaKey, MetaValue, MetaProp],
+This <: RichPropertyBase[Key, Value, MetaKey, MetaValue, MetaProp]]
  extends PersistedProperty[Key, Value, This]
    with PersistedMultiRecordProperty[Id, Key, Value, This]
    with RichProperty[Key, Value, MetaKey, MetaValue, MetaProp, This]
    with PersistedElement[PropertyPathWithId[Id] with RecordPath[MetaKey]] { this: This =>

  def parent: MultiRecordPath[Id]

  override final def path: PropertyPathWithId[Id] with RecordPath[MetaKey] = new PropertyPathWithId(parent, id) with RecordPath[MetaKey]

}

trait NewVertex[
TypeId,
Key,
+Value,
MetaKey,
+MetaValue,
+MetaProp <: Property[MetaKey, MetaValue, MetaProp],
+Prop <: RichProperty[Key, Value, MetaKey, MetaValue, MetaProp, Prop]
] extends Vertex[TypeId, Key, Value, MetaKey, MetaValue, MetaProp, Prop]
  with NewElement {

  type TempId = Int

  def tempId: TempId

}

// TODO: New elements
