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

package ch.datascience.graph.elements.builders

import ch.datascience.graph.elements.BoxedOrValidValue
import ch.datascience.graph.elements.persistence.Path
import ch.datascience.graph.elements.persistence.impl.{ImplPersistedMultiRecordRichProperty, ImplPersistedRecordProperty}

/**
  * Created by johann on 19/05/17.
  */
class PersistedMultiRecordRichPropertyBuilder[Id, Key, Value: BoxedOrValidValue, MetaKey, MetaValue: BoxedOrValidValue]
  extends PropertyBuilder[Key, Value, ImplPersistedMultiRecordRichProperty[Id, Key, Value, MetaKey, MetaValue]]
    with RecordBuilder[MetaKey, MetaValue, ImplPersistedRecordProperty[MetaKey, MetaValue], PersistedRecordPropertyBuilder[MetaKey, MetaValue], ImplPersistedMultiRecordRichProperty[Id, Key, Value, MetaKey, MetaValue]] {

  private[this] var myParent: Option[Path] = None

  private[this] var myId: Option[Id] = None

  def parent: Option[Path] = myParent

  def id: Option[Id] = myId

  def parent_=(parent: Path): this.type = {
    myParent = Some(parent)
    this
  }

  def id_=(id: Id): this.type = {
    myId = Some(id)
    this
  }

  object newProperty extends Builder[PersistedRecordPropertyBuilder[MetaKey, MetaValue]] {
    def isReady: Boolean = true
    def result(): PersistedRecordPropertyBuilder[MetaKey, MetaValue] = new PersistedRecordPropertyBuilder[MetaKey, MetaValue]()
  }

  def isReady: Boolean = (parent, id, key, value) match {
    case (Some(_), Some(_), Some(_), Some(_)) => true
    case _ => false
  }

  def result(): ImplPersistedMultiRecordRichProperty[Id, Key, Value, MetaKey, MetaValue] = (parent, id, key, value) match {
    case (Some(p), Some(i), Some(k), Some(v)) =>
      // Build a temporary version to extract the path
      val temp = ImplPersistedMultiRecordRichProperty(p, i, k, v, Map.empty[MetaKey, ImplPersistedRecordProperty[MetaKey, MetaValue]])
      val thisPath = temp.path

      val propertiesResult = for {
        (metaKey, metaPropBuilder) <- properties
      } yield metaKey -> (metaPropBuilder.parent = thisPath).result()

      ImplPersistedMultiRecordRichProperty[Id, Key, Value, MetaKey, MetaValue](p, i, k, v, propertiesResult)
    case _ => notReady()
  }

}
