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

package ch.datascience.graph.elements.persisted

import ch.datascience.graph.bases.HasId
import ch.datascience.graph.elements.Property
import ch.datascience.graph.elements.persisted.impl.ImplPersistedRecordLeafProperty

/**
  * Created by johann on 29/05/17.
  */
sealed trait PersistedProperty
  extends Property
    with PersistedElement {

  type PathType <: PropertyPath

  def parent: Path

}

trait PersistedRecordProperty
  extends PersistedProperty
    with PersistedElement {

  final type PathType = PropertyPathFromRecord

  final def path: PropertyPathFromRecord = PropertyPathFromRecord(parent, key)

}

trait PersistedMultiRecordProperty
  extends PersistedProperty
    with PersistedElement
    with HasId {

//  final type PathType = PropertyPathFromMultiRecord[Id]
  type PathType <: PropertyPathFromMultiRecord[Id]

//  final def path: PropertyPathFromMultiRecord[Id] = PropertyPathFromMultiRecord(parent, id)

}

object PersistedRecordProperty {

  def apply(
    parent: Path,
    key: PersistedRecordProperty#Key,
    value: PersistedRecordProperty#Value
  ): PersistedRecordProperty = ImplPersistedRecordLeafProperty(parent, key, value)

  def unapply(prop: PersistedRecordProperty): Option[(Path, PersistedRecordProperty#Key, PersistedRecordProperty#Value)] = {
    if (prop eq null)
      None
    else
      Some(prop.parent, prop.key, prop.value)
  }

}
