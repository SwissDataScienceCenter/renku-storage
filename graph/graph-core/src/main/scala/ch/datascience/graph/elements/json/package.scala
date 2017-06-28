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

package ch.datascience.graph.elements

import ch.datascience.graph.Constants
import ch.datascience.graph.naming.json.{NamespaceAndNameFormat, StringFormat}
import ch.datascience.graph.values.json.BoxedValueFormat
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

/**
  * Created by johann on 30/05/17.
  */
package object json {

  lazy val KeyFormat: StringFormat[Constants.Key] = NamespaceAndNameFormat
  lazy val TypeIdFormat: StringFormat[Constants.TypeId] = NamespaceAndNameFormat
  lazy val EdgeLabelFormat: StringFormat[Constants.EdgeLabel] = NamespaceAndNameFormat

  lazy val PropertyFormat: Format[Property] = (
      (JsPath \ "key").format[Property#Key](KeyFormat) and
        JsPath.format[Property#Value](BoxedValueFormat)
    )(
      { (k, v) =>
        new Property {
          def key: Key = k
          def value: Value = v
        }
      },
      unlift(Property.unapply)
    )

}
