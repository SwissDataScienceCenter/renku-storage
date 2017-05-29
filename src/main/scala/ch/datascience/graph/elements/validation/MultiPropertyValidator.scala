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

package ch.datascience.graph.elements.validation

import ch.datascience.graph.elements.{MultiPropertyValue, Property}
import ch.datascience.graph.scope.PropertyScope
import ch.datascience.graph.types.PropertyKey

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 01/05/17.
  */
trait MultiPropertyValidator {

  def validateMultiProperty(
    property: MultiPropertyValue[Property]
  )(
    implicit ec: ExecutionContext
  ): Future[ValidationResult[ValidatedMultiProperty]] = {
    val future = propertyScope getPropertyFor property.key
    future.map({ definition =>
      this.validateMultiPropertySync(property, definition)
    })(ec)
  }

  def validateMultiPropertySync(
    property: MultiPropertyValue[Property],
    definition: Option[PropertyKey]
  ): ValidationResult[ValidatedMultiProperty] = definition match {
    case None => Left(UnknownProperty(property.key))
    case Some(propertyKey) if property.key != propertyKey.key => Left(WrongDefinition(propertyKey.key, property.key))
    case Some(propertyKey) if property.dataType != propertyKey.dataType => Left(BadDataType(property.key, propertyKey.dataType, property.dataType))
    case Some(propertyKey) if property.cardinality != propertyKey.cardinality => Left(BadCardinality(property.key, propertyKey.cardinality, property.cardinality))
    case Some(propertyKey) => Right(Result(property, propertyKey))
  }

  protected def propertyScope: PropertyScope

  private[this] case class Result(
    properties: MultiPropertyValue[Property],
    propertyKey: PropertyKey
  ) extends ValidatedMultiProperty

}
