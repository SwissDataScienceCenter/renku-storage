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

import ch.datascience.graph.elements.{BoxedOrValidValue, Property, Record}
import ch.datascience.graph.types.{PropertyKey, RecordType}

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 08/05/17.
  */
trait RecordValidator[Key, Value, Prop <: Property[Key, Value, Prop]] { this: PropertyValidator[Key, Value, Prop] =>

  def validateRecord(
    record: Record[Key, Value, Prop]
  )(
    implicit e: BoxedOrValidValue[Value],
    ec: ExecutionContext
  ): Future[ValidationResult[ValidatedRecord[Key, Value, Prop]]] = {
    val future = propertyScope.getPropertiesFor(record.properties.keySet)
    future.map({ definitions =>
      this.validateRecordSync(record, definitions)
    })(ec)
  }

  def validateRecordSync(
    record: Record[Key, Value, Prop],
    definitions: Map[Key, PropertyKey[Key]]
  )(
    implicit e: BoxedOrValidValue[Value]
  ): ValidationResult[ValidatedRecord[Key, Value, Prop]] = {
    // First validate that the key -> property mapping is consistent
    val consistencyErrors = for {
      (key, property) <- record.properties
      if key != property.key
    } yield BadRecord(key, property.key)

    // Use PropertyValidator method validatePropertySync to validate each property
    val validatedProperties = for {
      (key, property) <- record.properties
      definition = definitions.get(key)
    } yield key -> this.validatePropertySync(property, definition)(e)
    val invalidPropertyErrors = validatedProperties.values.flatMap(_.left.toOption)

    // Collect errors
    val allErrors = consistencyErrors ++ invalidPropertyErrors

    if (allErrors.isEmpty) {
      val validProperties = for {
        (key, validated) <- validatedProperties
        v <- validated.right.toOption
      } yield key -> v.propertyKey
      Right(Result(record, RecordType(record.properties.keySet), validProperties))
    }
    else
      Left(MultipleErrors.make(allErrors.toSeq))
  }

  private[this] case class Result(
    record: Record[Key, Value, Prop],
    recordType: RecordType[Key],
    propertyKeys: Map[Key, PropertyKey[Key]]
  ) extends ValidatedRecord[Key, Value, Prop]

}
