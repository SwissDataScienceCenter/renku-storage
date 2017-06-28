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

package ch.datascience.graph.types.persistence.model

import java.util.UUID

import ch.datascience.graph.naming.NamespaceAndName
import ch.datascience.graph.types.{Cardinality, DataType, Multiplicity}
import play.api.libs.json.{Format, Reads}

/**
  * Created by johann on 13/06/17.
  */
package object json {

  implicit lazy val UUIDFormat: Format[UUID] = UUIDMappers.UUIDFormat
  lazy val notUUidReads: Reads[String] = UUIDMappers.notUUidReads

  implicit lazy val GraphDomainFormat: Format[GraphDomain] = GraphDomainMappers.GraphDomainFormat
  lazy val GraphDomainRequestFormat: Format[String] = GraphDomainMappers.GraphDomainRequestFormat

  implicit lazy val PropertyKeyFormat: Format[RichPropertyKey] = PropertyKeyMappers.PropertyKeyFormat
  lazy val PropertyKeyRequestFormat: Format[(String, String, DataType, Cardinality)] = PropertyKeyMappers.PropertyKeyRequestFormat

  implicit lazy val SystemPropertyKeyFormat: Format[SystemPropertyKey] = SystemPropertyKeyMappers.SystemPropertyKeyFormat
  lazy val SystemPropertyKeyRequestFormat: Format[(String, DataType, Cardinality)] = SystemPropertyKeyMappers.SystemPropertyKeyRequestFormat

  implicit lazy val EdgeLabelFormat: Format[RichEdgeLabel] = EdgeLabelMappers.EdgeLabelFormat
  lazy val EdgeLabelRequestFormat: Format[(String, String, Multiplicity)] = EdgeLabelMappers.EdgeLabelRequestFormat

  implicit lazy val NamedTypeFormat: Format[RichNamedType] = NamedTypeMappers.NamedTypeFormat
  lazy val NamedTypeRequestFormat: Format[(String, String, Seq[NamespaceAndName], Seq[NamespaceAndName])] = NamedTypeMappers.NamedTypeRequestFormat

}
