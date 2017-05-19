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

import ch.datascience.graph.elements.persistence.impl.{ImplPersistedMultiRecordRichProperty, ImplPersistedRecordProperty}
import ch.datascience.graph.elements.{BoxedOrValidValue, Properties, Property, ValidValue}

/**
  * Created by johann on 18/05/17.
  */
class PersistedMultiRecordRichPropertyBuilder[+Id, +Key, +Value: BoxedOrValidValue, MetaKey, MetaValue: BoxedOrValidValue](
  val id: Id,
  val key: Key,
  val value: Value
) {

  private[this] var myProperties: Map[MetaKey, PersistedRecordPropertyBuilder[MetaKey, MetaValue]] = Map.empty

  def properties: Map[MetaKey, PersistedRecordPropertyBuilder[MetaKey, MetaValue]] = myProperties

  def +=(metaKeyMetaValue: (MetaKey, MetaValue)): this.type = addProperty(metaKeyMetaValue._1, metaKeyMetaValue._2)

  def addProperty(metaKey: MetaKey, metaValue: MetaValue): this.type = {
    val builder = new PersistedRecordPropertyBuilder(metaKey, metaValue)
    myProperties += builder.key -> builder
    this
  }

  def result(parent: Path): ImplPersistedMultiRecordRichProperty[Id, Key, Value, MetaKey, MetaValue] = {
    // Build a temporary version to extract the path
    val temp = ImplPersistedMultiRecordRichProperty(parent, id, key, value, Map.empty[MetaKey, ImplPersistedRecordProperty[MetaKey, MetaValue]])
    val thisPath = temp.path

    val properties = for {
      (key, builder) <- myProperties
      property = builder.result(thisPath)
    } yield key -> property

    ImplPersistedMultiRecordRichProperty(parent, id, key, value, properties)
  }

}
