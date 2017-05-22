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

import ch.datascience.graph.elements.Property
import ch.datascience.graph.types.PropertyKey
import ch.datascience.graph.scope.PropertyScope
import ch.datascience.graph.values.BoxedOrValidValue

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 01/05/17.
  */
trait PropertyValidator[Key, Value, Prop <: Property[Key, Value]] {

  def validateProperty(
    property: Prop
  )(
    implicit e: BoxedOrValidValue[Value],
    ec: ExecutionContext
  ): Future[ValidationResult[ValidatedProperty[Key, Value, Prop]]] = {
    val future = propertyScope getPropertyFor property.key
    future.map({ definition =>
      this.validatePropertySync(property, definition)(e)
    })(ec)
  }

  def validatePropertySync(
    property: Prop,
    definition: Option[PropertyKey[Key]]
  )(
    implicit e: BoxedOrValidValue[Value]
  ): ValidationResult[ValidatedProperty[Key, Value, Prop]] = definition match {
    case None => Left(UnknownProperty(property.key))
    case Some(propertyKey) if property.key != propertyKey.key => Left(WrongDefinition(propertyKey.key, property.key))
    case Some(propertyKey) if property.dataType(e) != propertyKey.dataType => Left(BadDataType(property.key, propertyKey.dataType, property.dataType(e)))
    case Some(propertyKey) => Right(Result(property, propertyKey))
  }

  protected def propertyScope: PropertyScope[Key]

  private[this] case class Result(
    property: Prop,
    propertyKey: PropertyKey[Key]
  ) extends ValidatedProperty[Key, Value, Prop]

}
