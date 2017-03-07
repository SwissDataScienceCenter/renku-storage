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

package ch.datascience.typesystem

/**
  * Created by johann on 07/03/17.
  */
sealed abstract class DataType

object DataType {

  case object String extends DataType
  case object Character extends DataType
  case object Boolean extends DataType
  case object Byte extends DataType
  case object Short extends DataType
  case object Integer extends DataType
  case object Long extends DataType
  case object Float extends DataType
  case object Double extends DataType
  case object Decimal extends DataType
  case object Precision extends DataType
  case object Date extends DataType
  case object Geoshape extends DataType
  case object UUID extends DataType

}
