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
import ch.datascience.graph.values.BoxedValue
import play.api.libs.json.{JsResult, JsValue, Reads}


/**
  * Created by johann on 24/05/17.
  */
case class ValueReads(dataType: DataType) extends Reads[BoxedValue] {

  def reads(json: JsValue): JsResult[BoxedValue] = dataType match {
    case DataType.String    => implicitly[Reads[String]].reads(json) map BoxedValue.apply
    case DataType.Character => implicitly[Reads[String]].reads(json) map { str => BoxedValue.apply(str.head) }
    case DataType.Boolean   => implicitly[Reads[Boolean]].reads(json) map BoxedValue.apply
    case DataType.Byte      => implicitly[Reads[Byte]].reads(json) map BoxedValue.apply
    case DataType.Short     => implicitly[Reads[Short]].reads(json) map BoxedValue.apply
    case DataType.Integer   => implicitly[Reads[Int]].reads(json) map BoxedValue.apply
    case DataType.Long      => implicitly[Reads[Long]].reads(json) map BoxedValue.apply
    case DataType.Float     => implicitly[Reads[Float]].reads(json) map BoxedValue.apply
    case DataType.Double    => implicitly[Reads[Double]].reads(json) map BoxedValue.apply
    case DataType.UUID      => implicitly[Reads[UUID]].reads(json) map BoxedValue.apply
  }

}
