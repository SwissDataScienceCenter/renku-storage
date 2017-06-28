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

package ch.datascience.graph.scope.persistence.remote

import ch.datascience.graph.scope.persistence.PersistedNamedTypes
import ch.datascience.graph.scope.persistence.json.FetchNamedTypesForFormats
import ch.datascience.graph.types.NamedType
import ch.datascience.graph.types.json.NamedTypeFormat
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import play.api.libs.ws.WSResponse

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 17/05/17.
  */
trait RemotePersistedNamedTypes extends PersistedNamedTypes {

  // TODO: clean this

  /**
    * Fetches named type with specified key
    *
    * @param typeId
    * @return a future containing some named type if a corresponding one is found, None otherwise
    */
  final def fetchNamedTypeFor(typeId: NamedType#TypeId): Future[Option[NamedType]] = {
    for {
      response <- client.fetchNamedTypeForRemoteCall(typeId)
    } yield response.status match {
      case 200 =>
        val result = response.json.validate[NamedType](NamedTypeFormat)
        result match {
          case JsSuccess(propertyKey, _) => Some(propertyKey)
          case JsError(e) => throw JsResultException(e)
        }
      case 404 => None
      case _ => cannotHandleResponse2(response)
    }
  }

  /**
    * Grouped version of fetchNamedTypeFor
    *
    * If some keys are not found, they will not be part of the result map
    *
    * @param typeIds set of keys to retrieve
    * @return map key -> named type, will not contain unknown keys
    */
  final def fetchNamedTypesFor(typeIds: Set[NamedType#TypeId]): Future[Map[NamedType#TypeId, NamedType]] = {
    for {
      response <- client.fetchNamedTypesForRemoteCall(typeIds)
    } yield response.status match {
      case 200 =>
        val result = response.json.validate[Map[NamedType#TypeId, NamedType]](FetchNamedTypesForFormats.ResponseFormat)
        result match {
          case JsSuccess(namedTypes, _) => namedTypes
          case JsError(e) => throw JsResultException(e)
        }
      case _ => cannotHandleResponse2(response)
    }
  }

  protected def client: ConfiguredClient

  protected def executionContext: ExecutionContext

  implicit lazy val ec2: ExecutionContext = executionContext

  protected def cannotHandleResponse2(response: WSResponse): Nothing = {
    throw new RuntimeException(s"Unexpected answer: HTTP${response.status} - ${response.statusText}, ${response.body}")
  }

}
