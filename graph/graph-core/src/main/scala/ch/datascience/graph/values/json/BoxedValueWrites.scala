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

package ch.datascience.graph.values.json

import java.util.UUID

import ch.datascience.graph.types.DataType
import ch.datascience.graph.types.json.DataTypeFormat
import ch.datascience.graph.values._
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by johann on 24/05/17.
  */
object BoxedValueWrites extends Writes[BoxedValue] {

  def writes(value: BoxedValue): JsValue = self.writes(value)

  private[this] lazy val self: Writes[BoxedValue] = (
    (JsPath \ "data_type").write[DataType](DataTypeFormat) and
      (JsPath \ "value").write[BoxedValue](valueWrites)
  ) { b: BoxedValue => (b.dataType, b) }

  private[this] object valueWrites extends Writes[BoxedValue] {
    def writes(value: BoxedValue): JsValue = value match {
      case StringValue(v)  => implicitly[Writes[String]].writes(v)
      case CharValue(v)    => implicitly[Writes[String]].writes(v.toString)
      case BooleanValue(v) => implicitly[Writes[Boolean]].writes(v)
      case ByteValue(v)    => implicitly[Writes[Byte]].writes(v)
      case ShortValue(v)   => implicitly[Writes[Short]].writes(v)
      case IntValue(v)     => implicitly[Writes[Int]].writes(v)
      case LongValue(v)    => implicitly[Writes[Long]].writes(v)
      case FloatValue(v)   => implicitly[Writes[Float]].writes(v)
      case DoubleValue(v)  => implicitly[Writes[Double]].writes(v)
      case UuidValue(v)    => implicitly[Writes[UUID]].writes(v)
    }
  }

}
