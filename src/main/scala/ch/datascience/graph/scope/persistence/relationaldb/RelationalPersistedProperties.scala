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

package ch.datascience.graph.scope.persistence.relationaldb

import ch.datascience.graph.naming.NamespaceAndName
import ch.datascience.graph.scope.persistence.PersistedProperties
import ch.datascience.graph.types.PropertyKey

import scala.concurrent.Future

/**
  * Created by johann on 09/05/17.
  */
trait RelationalPersistedProperties extends PersistedProperties { this: ExecutionComponent with OrchestrationComponent =>

  /**
    * Fetches property key with specified key
    *
    * @param key
    * @return a future containing some property key if a corresponding one is found, None otherwise
    */
  def fetchPropertyFor(key: NamespaceAndName): Future[Option[PropertyKey]] = for {
    opt <- orchestrator.propertyKeys.findByNamespaceAndName(key)
  } yield for {
    propertyKey <- opt
  } yield propertyKey.toStandardPropertyKey

  /**
    * Grouped version of getPropertyFor
    *
    * If some keys are not found, they will not be part of the result map
    *
    * @param keys set of keys to retrieve
    * @return map key -> property key, will not contain unknown keys
    */
  def fetchPropertiesFor(keys: Set[NamespaceAndName]): Future[Map[PropertyKey#Key, PropertyKey]] = {
    val futurePropertyKeys = Future.traverse(keys.toIterable) { key =>
      for {
        opt <- orchestrator.propertyKeys.findByNamespaceAndName(key)
      } yield key -> opt
    }

    for {
      propertyKeys <- futurePropertyKeys
      iterable = for {
        (key, opt) <- propertyKeys
        propertyKey <- opt
      } yield key -> propertyKey.toStandardPropertyKey
    } yield iterable.toMap
  }

}
