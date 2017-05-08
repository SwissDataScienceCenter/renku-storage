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

package ch.datascience.graph.typevalidation

import ch.datascience.graph.elements.Property
import ch.datascience.graph.types.DataType

/**
  * Created by johann on 01/05/17.
  */
sealed trait ValidationError {

}

final case class MultipleErrors(errors: Seq[ValidationError]) extends ValidationError

final case class UnknownProperty[+Key](key: Key) extends ValidationError

final case class BadDataType[+Key, +Value, +Prop <: Property[Key, Value, Prop]](
  value   : Value,
  required: DataType,
  found   : DataType
) extends ValidationError

final case class BadRecord[+Key, +Value, +Prop <: Property[Key, Value, Prop]](
  required: Key,
  found: Key
) extends ValidationError


object MultipleErrors {

  def make(errors: Seq[ValidationError]): ValidationError = errors.size match {
    case 1 => errors.head
    case _ => new MultipleErrors(errors)
  }

}
