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

package ch.datascience.graph.init

import ch.datascience.graph.Constants
import ch.datascience.graph.types.{EdgeLabel, NamedType, PropertyKey}

/**
  * Created by johann on 21/06/17.
  */
case class TypeInit(
  systemPropertyKeys: List[SystemPropertyKey],
  propertyKeys: List[PropertyKey],
  namedTypes: List[NamedType],
  edgeLabels: List[EdgeLabel]
) {

  def graphDomains: Seq[String] = {
    val gd1 = (for {
      pk <- propertyKeys
    } yield pk.key.namespace).toSet

    val gd2 = (for {
      nt <- namedTypes
    } yield nt.typeId.namespace).toSet

    val gd3 = (for {
      el <- edgeLabels
    } yield el.key.namespace).toSet

    (gd1 ++ gd2 ++ gd3).toSeq
  }

}
