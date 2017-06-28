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

package ch.datascience.graph.scope

import ch.datascience.graph.types.PropertyKey
import ch.datascience.graph.scope.persistence.PersistedProperties

import scala.collection
import scala.collection.concurrent
import scala.concurrent.{ExecutionContext, Future}

/**
  * Base trait for scopes that handle property definitions
  * @tparam Key type of key
  */
trait PropertyScope {

  final def getPropertyFor(key: PropertyKey#Key)(implicit ec: ExecutionContext): Future[Option[PropertyKey]] = {
    propertyDefinitions get key match {
      case Some(propertyKey) => Future.successful( Some(propertyKey) )
      case None => {
        val result = persistedProperties.fetchPropertyFor(key)

        // If we get a property key, then we add it to our scope
        result.onSuccess({
          case Some(propertyKey) => propertyDefinitions.put(propertyKey.key, propertyKey)
        })(ec)

        result
      }
    }
  }

  final def getPropertiesFor(keys: Set[PropertyKey#Key])(implicit ec: ExecutionContext): Future[Map[PropertyKey#Key, PropertyKey]] = {
    // Locally, sort known keys and unkown keys
    val tryLocally = (for (key <- keys) yield key -> propertyDefinitions.get(key)).toMap
    val knownPropertyKeys: Map[PropertyKey#Key, PropertyKey] = for {
      (key, opt) <- tryLocally
      propertyKey <- opt
    } yield key -> propertyKey
    val unknownKeys: Set[PropertyKey#Key] = for {
      key <- tryLocally.keySet
      if tryLocally(key).isEmpty
    } yield key

    // Resolve unknown keys
    val resolved = persistedProperties.fetchPropertiesFor(unknownKeys)

    // Update resolved keys
    resolved.map({ definitions =>
      for (propertyKey <- definitions.values) {
        propertyDefinitions.put(propertyKey.key, propertyKey)
      }
    })(ec)

    resolved.map({ definitions =>
      knownPropertyKeys ++ definitions
    })(ec)
  }

  final def getCachedProperties: Future[collection.Map[PropertyKey#Key, PropertyKey]] = {
    Future.successful( propertyDefinitions.readOnlySnapshot() )
  }

  protected def propertyDefinitions: concurrent.TrieMap[PropertyKey#Key, PropertyKey]

  protected def persistedProperties: PersistedProperties

}
