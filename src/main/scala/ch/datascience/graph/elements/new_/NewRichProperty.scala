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

package ch.datascience.graph.elements.new_

import ch.datascience.graph.elements.RichProperty
import ch.datascience.graph.elements.detached.DetachedProperty
import ch.datascience.graph.elements.new_.impl.ImplNewRichProperty
import ch.datascience.graph.elements.persisted.Path

/**
  * Created by johann on 29/05/17.
  */
trait NewRichProperty extends NewProperty with RichProperty {

  final type Prop = DetachedProperty

}

object NewRichProperty {

  def apply(
    parent: Path,
    key: NewRichProperty#Key,
    value: NewRichProperty#Value,
    properties: NewRichProperty#Properties
  ): NewRichProperty = ImplNewRichProperty(parent, key, value, properties)

  def unapply(prop: NewRichProperty): Option[(Path, NewRichProperty#Key, NewRichProperty#Value, NewRichProperty#Properties)] = {
    if (prop eq null)
      None
    else
      Some(prop.parent, prop.key, prop.value, prop.properties)
  }

}
