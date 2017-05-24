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

package ch.datascience.graph.scope.persistence

import ch.datascience.graph.types.{StandardPropKey, StandardPropertyKey}
import play.api.libs.json.{Reads, Writes}
import ch.datascience.graph.types.json.{standardPropKeyReads, standardPropKeyWrites}

/**
  * Created by johann on 24/05/17.
  */
package object json {

  implicit lazy val standardFetchPropertiesForQueryReads: Reads[Set[StandardPropKey]] = new FetchPropertiesForQueryReads[StandardPropKey]
  implicit lazy val standardFetchPropertiesForQueryWrites: Writes[Set[StandardPropKey]] = new FetchPropertiesForQueryWrites[StandardPropKey]
  implicit lazy val standardFetchPropertiesForResponseReads: Reads[Map[StandardPropKey, StandardPropertyKey]] = new FetchPropertiesForResponseReads[StandardPropKey]
  implicit lazy val standardFetchPropertiesForResponseWrites: Writes[Map[StandardPropKey, StandardPropertyKey]] = new FetchPropertiesForResponseWrites[StandardPropKey]

}
