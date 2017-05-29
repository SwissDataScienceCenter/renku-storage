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

import ch.datascience.graph.types.{NamedType, PropertyKey}

import scala.concurrent.Future

/**
  * Created by johann on 16/05/17.
  */
trait PersistedNamedTypes {

  /**
    * Fetches named type with specified key
    * @param typeId
    * @return a future containing some named type if a corresponding one is found, None otherwise
    */
  def fetchNamedTypeFor(typeId: NamedType#TypeId): Future[Option[NamedType]]


  /**
    * Grouped version of fetchNamedTypeFor
    *
    * If some keys are not found, they will not be part of the result map
    * @param typeIds set of keys to retrieve
    * @return map key -> named type, will not contain unknown keys
    */
  def fetchNamedTypesFor(typeIds: Set[NamedType#TypeId]): Future[Map[NamedType#TypeId, NamedType]]
  
}
