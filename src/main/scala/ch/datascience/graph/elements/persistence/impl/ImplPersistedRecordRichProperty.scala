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

package ch.datascience.graph.elements.persistence.impl

import ch.datascience.graph.elements.persistence.{PersistedRichRecordProperty, RecordPath}
import ch.datascience.graph.elements.{BoxedOrValidValue, Properties, RichProperty}

/**
  * Created by johann on 11/05/17.
  */
case class ImplPersistedRecordRichProperty[+Key, +Value: BoxedOrValidValue, MetaKey, +MetaValue: BoxedOrValidValue](
  parent: RecordPath,
  key: Key,
  value: Value,
  properties: Properties[MetaKey, MetaValue, ImplPersistedRecordProperty[MetaKey, MetaValue]]
) extends PersistedRichRecordProperty[Key, Value, ImplPersistedRecordRichProperty[Key, Value, MetaKey, MetaValue]]
  with RichProperty[Key, Value, MetaKey, MetaValue, ImplPersistedRecordProperty[MetaKey, MetaValue], ImplPersistedRecordRichProperty[Key, Value, MetaKey, MetaValue]]
