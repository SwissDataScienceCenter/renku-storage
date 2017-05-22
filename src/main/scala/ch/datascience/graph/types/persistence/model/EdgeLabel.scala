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

import ch.datascience.graph.naming.{Name, NamespaceAndName}
import ch.datascience.graph.types.persistence.model.relational.{RowEdgeLabel, RowGraphDomain}
import org.janusgraph.core.Multiplicity

/**
  * Created by johann on 16/03/17.
  */
case class EdgeLabel(id: UUID,
                     graphDomain: GraphDomain,
                     name: String,
                     multiplicity: Multiplicity = Multiplicity.SIMPLE)
  extends AbstractEntity[RowEdgeLabel] {
  Name(name)
//  require(NamespaceAndName.nameIsValid(name), s"Invalid name: '$name' (Pattern: ${NamespaceAndName.namePattern})")

  def namespace: String = graphDomain.namespace

  def key: NamespaceAndName = NamespaceAndName(namespace, name)

  def toRow: RowEdgeLabel = RowEdgeLabel(id, graphDomain.id, name, multiplicity)

  final override val entityType: EntityType = EntityType.EdgeLabel

}

//object EdgeLabel {
//
//  def make(rowGraphDomain: RowGraphDomain, rowEdgeLabel: RowEdgeLabel): EdgeLabel = {
//    val graphDomain = GraphDomain.make(rowGraphDomain)
//    EdgeLabel(rowEdgeLabel.id, graphDomain, rowEdgeLabel.name, rowEdgeLabel.multiplicity)
//  }
//
//  def tupled: ((UUID, GraphDomain, String, Multiplicity)) => EdgeLabel = (EdgeLabel.apply _).tupled
//
//}
