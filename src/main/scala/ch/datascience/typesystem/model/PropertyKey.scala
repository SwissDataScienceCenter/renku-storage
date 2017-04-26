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

package ch.datascience.typesystem
package model

import java.util.UUID

import ch.datascience.typesystem.model.base.PropertyKeyBase
import ch.datascience.typesystem.model.relational.{PropertyKeyBase => RelationalPropertyKey}


/**
  * Created by johann on 24/04/17.
  */
case class PropertyKey(
                        id: UUID,
                        graphDomain: GraphDomain,
                        name: String,
                        dataType: DataType,
                        cardinality: Cardinality
                      )
  extends RelationalPropertyKey
    with PropertyKeyBase[String] {

  lazy val namespace: String = graphDomain.namespace

  override lazy val key: String = s"$namespace:$name"

  override lazy val graphDomainId: UUID = graphDomain.id

}

object PropertyKey {

  def fromRelational(graphDomain: GraphDomain, relationalPropertyKey: RelationalPropertyKey): PropertyKey = {
    PropertyKey(relationalPropertyKey.id, graphDomain, relationalPropertyKey.name, relationalPropertyKey.dataType, relationalPropertyKey.cardinality)
  }

  def tupled: (((UUID, GraphDomain, String, DataType, Cardinality)) => PropertyKey) = (PropertyKey.apply _).tupled

}
