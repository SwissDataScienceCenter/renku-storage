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

package ch.datascience.typesystem.model

import ch.datascience.typesystem.{Cardinality, DataType, PropertyDefinition => PropertyKeyBase}

import scala.util.matching.Regex

/**
  * Created by johann on 24/04/17.
  */
case class PropertyKey(namespace: String,
                       name: String,
                       dataType: DataType = DataType.String,
                       cardinality: Cardinality = Cardinality.Single)
  extends PropertyKeyBase[String] {

  override lazy val key: String = s"$namespace:$name"

}

//object PropertyKey {
//
//  lazy val fqdnRegex: Regex = "([^:]*):(.*)".r
//
//  def apply(fqdn: String, dataType: DataType, cardinality: Cardinality): PropertyKey = fqdn match {
//    case fqdnRegex(namespace, name) => PropertyKey(namespace, name, dataType, cardinality)
//  }
//
//}
