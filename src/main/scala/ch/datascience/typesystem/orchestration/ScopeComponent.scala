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

package ch.datascience.typesystem.orchestration

import java.util.UUID

import ch.datascience.typesystem.scope.StandardScope

import scala.concurrent.Future

/**
  * Created by johann on 24/04/17.
  */
trait ScopeComponent { this: DatabaseComponent with ExecutionComponent =>

  import profile.api._

  type Scope = StandardScope[String, String]

  private[this] var scope: Scope = StandardScope.empty[String, String]

  def getCurrentScopeSync: Scope = synchronized {
    scope
  }

  def getCurrentScope: Future[Scope] = Future { getCurrentScopeSync }

  def scopeForPropertyKey(namespace: String, name: String): Future[Scope] = {
    val candidateScope = getCurrentScope
    val key = s"$namespace:$name"
    candidateScope flatMap { scope =>
      scope.propertyDefinitions contains key match {
        case true => Future.successful(scope)
        case false =>
          val select = dal.propertyKeys.findByNamespaceAndNameAsModel(namespace, name).result.headOption
          db.run(select) map {
            case Some(propertyKey) =>
              val newScope = scope + propertyKey
              synchronized {
                this.scope = newScope
              }
              newScope
            case None => scope
          }
      }
    }
  }

}
