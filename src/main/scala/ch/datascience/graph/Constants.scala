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

package ch.datascience.graph

import ch.datascience.graph.naming.NamespaceAndName
import ch.datascience.graph.values.BoxedValue

/**
  * Created by johann on 24/05/17.
  */
object Constants {

  /**
    * Type of property keys
    */
//  type StandardKey = NamespaceAndName
  type Key = NamespaceAndName

  /**
    * Type of named type identifiers
    */
//  type StandardTypeId = NamespaceAndName
  type TypeId = NamespaceAndName

  /**
    * Type of property values
    */
//  type StandardValue = BoxedValue
  type Value = BoxedValue

  /**
    * Type of edge labels
    */
  type EdgeLabel = NamespaceAndName

  /**
    * Type of vertex ids
    */
  type VertexId = Long

  /**
    * Type of edge ids
    */
  type EdgeId = String

  /**
    * Type of vertex properties
    */
  type VertexPropertyId = String

  lazy val TypeKey: String = "type"

}
