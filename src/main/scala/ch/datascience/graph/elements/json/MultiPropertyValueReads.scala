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

package ch.datascience.graph.elements.json

import ch.datascience.graph.elements._
import ch.datascience.graph.types.json.{CardinalityFormat, DataTypeFormat}
import ch.datascience.graph.types.{Cardinality, DataType}
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by johann on 31/05/17.
  */
class MultiPropertyValueReads[P <: Property : Reads] extends Reads[MultiPropertyValue[P]] {

  def reads(json: JsValue): JsResult[MultiPropertyValue[P]] = self.reads(json).flatMap {
    case (key, dataType, cardinality, props) => try {
      cardinality match {
        case Cardinality.Single => JsSuccess(SingleValue[P](props.head))
        case Cardinality.Set => JsSuccess(SetValue[P](props.toList))
        case Cardinality.List => JsSuccess(ListValue[P](props.toList))
      }
    } catch {
      case e: IllegalArgumentException => JsError(e.getMessage)
    }
  }

  private[this] lazy val self: Reads[(P#Key, DataType, Cardinality, Seq[P])] = (
    (JsPath \ "key").read[P#Key](KeyFormat) and
      (JsPath \ "data_type").read[DataType](DataTypeFormat) and
      (JsPath \ "cardinality").read[Cardinality](CardinalityFormat) and
      (JsPath \ "values").read[Seq[P]]
    ) { (key, dataType, cardinality, props) => (key, dataType, cardinality, props) }

}
