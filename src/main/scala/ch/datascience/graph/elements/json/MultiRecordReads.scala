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

import ch.datascience.graph.elements.{MultiPropertyValue, MultiRecord, Property}
import play.api.libs.json._

/**
  * Created by johann on 31/05/17.
  */
class MultiRecordReads[P <: Property : Reads] extends Reads[MultiRecord { type Prop = P }] {

  def reads(json: JsValue): JsResult[MultiRecord {type Prop = P}] = {
    val result = for {
      opt <- (JsPath \ "properties").readNullable[Map[P#Key, MultiPropertyValue[P]]].reads(json)
      map = opt match {
        case Some(m) => m
        case _ => Map.empty[P#Key, MultiPropertyValue[P]]
      }
    } yield {
      new MultiRecord {
        type Prop = P
        def properties: Properties = map
      }
    }

    // Repath
    result match {
      case JsSuccess(x, _) => JsSuccess(x)
      case _ => result
    }
  }

//  private[this] implicit lazy val mapReads: Reads[Map[P#Key, MultiPropertyValue[P]]] = KeyFormat.mapReads[ MultiPropertyValue[P]](multiPropertyValueReads)
  private[this] implicit lazy val mapReads: Reads[Map[P#Key, MultiPropertyValue[P]]] = implicitly[Reads[Seq[MultiPropertyValue[P]]]].map { seq =>
    (for {
      prop <- seq
    } yield prop.key -> prop).toMap
  }

  private[this] implicit lazy val multiPropertyValueReads: MultiPropertyValueReads[P] = new MultiPropertyValueReads[P]

}
