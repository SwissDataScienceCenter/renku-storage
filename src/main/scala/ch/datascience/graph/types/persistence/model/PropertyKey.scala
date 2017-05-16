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

import ch.datascience.graph.NamespaceAndName
import ch.datascience.graph.types.persistence.model.relational.{RowGraphDomain, RowPropertyKey}
import ch.datascience.graph.types.{Cardinality, DataType, PropertyKey => PropertyKeyBase}

/**
  * Created by johann on 09/05/17.
  */
case class PropertyKey(
  id: UUID,
  graphDomain: GraphDomain,
  name: String,
  dataType: DataType,
  cardinality: Cardinality
) extends AbstractEntity[RowPropertyKey]
  with PropertyKeyBase[NamespaceAndName] {
  require(NamespaceAndName.nameIsValid(name), s"Invalid name: '$name' (Pattern: ${NamespaceAndName.namePattern})")

  def namespace: String = graphDomain.namespace

  def key: NamespaceAndName = NamespaceAndName(namespace, name)

  def toRow: RowPropertyKey = RowPropertyKey(id, graphDomain.id, name, dataType, cardinality)

  final override val entityType: EntityType = EntityType.PropertyKey

}

//object PropertyKey {
//
//  def make(rowGraphDomain: RowGraphDomain, rowPropertyKey: RowPropertyKey): PropertyKey = {
//    val graphDomain = GraphDomain.make(rowGraphDomain)
//    PropertyKey(rowPropertyKey.id, graphDomain, rowPropertyKey.name, rowPropertyKey.dataType, rowPropertyKey.cardinality)
//  }
//
//  def tupled: ((UUID, GraphDomain, String, DataType, Cardinality)) => PropertyKey = (PropertyKey.apply _).tupled
//
//}
