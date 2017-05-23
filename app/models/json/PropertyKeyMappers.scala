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

package models
package json

import java.util.UUID

import ch.datascience.graph.types.persistence.model.{GraphDomain, RichPropertyKey}
import ch.datascience.graph.types.{Cardinality, DataType}
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Created by johann on 26/04/17.
  */
object PropertyKeyMappers {

  def propertyKeyWrites: Writes[RichPropertyKey] = (
    (JsPath \ "id").write[UUID] and
      (JsPath \ "graphDomain").write[GraphDomain] and
      (JsPath \ "name").write[String] and
      (JsPath \ "dataType").write[DataType] and
      (JsPath \ "cardinality").write[Cardinality]
    )(unlift(RichPropertyKey.unapply))

}
