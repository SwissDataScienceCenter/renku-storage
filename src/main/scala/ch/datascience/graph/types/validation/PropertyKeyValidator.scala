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

package ch.datascience.graph.types.validation

import ch.datascience.graph.scope.PropertyScope
import ch.datascience.graph.types.PropertyKey

import scala.concurrent.Future

/**
  * Created by johann on 09/05/17.
  */
trait PropertyKeyValidator {

  def validatePropertyKey(propertyKey: PropertyKey): Future[ValidationResult[ValidatedPropertyKey]] = {
    Future.successful( validatePropertyKeySync(propertyKey) )
  }

  def validatePropertyKeySync(propertyKey: PropertyKey): ValidationResult[ValidatedPropertyKey] = {
    Right(Result(propertyKey))
  }

  private[this] case class Result(propertyKey: PropertyKey) extends ValidatedPropertyKey

}
