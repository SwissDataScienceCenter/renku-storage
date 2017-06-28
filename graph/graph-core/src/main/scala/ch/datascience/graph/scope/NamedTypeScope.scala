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

import ch.datascience.graph.scope.persistence.PersistedNamedTypes
import ch.datascience.graph.types.{NamedType, PropertyKey}

import scala.collection.concurrent
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 16/05/17.
  */
trait NamedTypeScope { this: PropertyScope =>

  final def getDefinitionsFor(typeId: NamedType#TypeId)(implicit ec: ExecutionContext): Future[(Map[PropertyKey#Key, PropertyKey], Map[NamedType#TypeId, NamedType])] = {
    getNamedTypeFor(typeId) flatMap {
      case Some(namedType) =>
        for {
          superTypesDefinitions <- getNamedTypesFor(namedType.superTypes)
          propertiesDefinition <- getPropertiesFor(namedType.properties)
        } yield (propertiesDefinition, superTypesDefinitions + (namedType.typeId -> namedType))
      case None => Future.successful((Map.empty, Map.empty))
    }
  }

  final def getDefinitionsFor(typeIds: Set[NamedType#TypeId])(implicit ec: ExecutionContext): Future[(Map[PropertyKey#Key, PropertyKey], Map[NamedType#TypeId, NamedType])] = {
    for {
      namedTypesDefinitions <- getNamedTypesFor(typeIds)
      allSuperTypes = namedTypesDefinitions.values.map(_.superTypes).reduce(_ ++ _)
      allProperties = namedTypesDefinitions.values.map(_.properties).reduce(_ ++ _)
      superTypesDefinitions <- getNamedTypesFor(allSuperTypes)
      propertiesDefinition <- getPropertiesFor(allProperties)
    } yield (propertiesDefinition, namedTypesDefinitions ++ superTypesDefinitions)
  }

  final def getNamedTypeFor(typeId: NamedType#TypeId)(implicit ec: ExecutionContext): Future[Option[NamedType]] = {
    namedTypeDefinitions get typeId match {
      case Some(namedType) => Future.successful( Some(namedType) )
      case None => {
        val result = persistedNamedTypes.fetchNamedTypeFor(typeId)

        // If we get a property key, then we add it to our scope
        result.onSuccess({
          case Some(namedType) => namedTypeDefinitions.put(namedType.typeId, namedType)
        })(ec)

        result
      }
    }
  }

  final def getNamedTypesFor(typeIds: Set[NamedType#TypeId])(implicit ec: ExecutionContext): Future[Map[NamedType#TypeId, NamedType]] = {
    // Locally, sort known keys and unkown keys
    val tryLocally = (for (typeId <- typeIds) yield typeId -> namedTypeDefinitions.get(typeId)).toMap
    val knownNamedTypes: Map[NamedType#TypeId, NamedType] = for {
      (key, opt) <- tryLocally
      namedType <- opt
    } yield key -> namedType
    val unknownKeys: Set[NamedType#TypeId] = for {
      key <- tryLocally.keySet
      if tryLocally(key).isEmpty
    } yield key

    // Resolve unknown keys
    val resolved = persistedNamedTypes.fetchNamedTypesFor(unknownKeys)

    // Update resolved keys
    resolved.map({ definitions =>
      for (namedType <- definitions.values) {
        namedTypeDefinitions.put(namedType.typeId, namedType)
      }
    })(ec)

    resolved.map({ definitions =>
      knownNamedTypes ++ definitions
    })(ec)
  }

  final def getCachedPropertiesAndNamedTypes: Future[(collection.Map[PropertyKey#Key, PropertyKey], collection.Map[NamedType#TypeId, NamedType])] = synchronized {
    Future.successful( (propertyDefinitions.readOnlySnapshot(), namedTypeDefinitions.readOnlySnapshot()) )
  }

  final def getCachedNamedTypes: Future[collection.Map[NamedType#TypeId, NamedType]] = {
    Future.successful( namedTypeDefinitions.readOnlySnapshot() )
  }

  protected def namedTypeDefinitions: concurrent.TrieMap[NamedType#TypeId, NamedType]

  protected def persistedNamedTypes: PersistedNamedTypes

}
