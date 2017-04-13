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

package ch.datascience.typesystem.model.table

import java.util.UUID

import ch.datascience.typesystem.model.EntityType
import ch.datascience.typesystem.model.row.{Entity, GraphDomain}
import slick.jdbc.JdbcProfile
import slick.lifted.{CompiledFunction, ForeignKeyQuery, Index, ProvenShape}

/**
  * Created by johann on 17/03/17.
  */
trait GraphDomainComponent { this: JdbcProfileComponent with EntityComponent with AbstractEntityComponent =>

  import profile.api._

  class GraphDomains(tag: Tag) extends Table[GraphDomain](tag, "GRAPH_DOMAINS") with AbstractEntityTable[GraphDomain] {

    // Columns
//    def id: Rep[UUID] = column[UUID]("UUID", O.PrimaryKey)

    def namespace: Rep[String] = column[String]("NAMESPACE")

    // Indexes
    def idx: Index = index("IDX_GRAPH_DOMAINS_NAMESPACE", namespace, unique = true)

    // Foreign keys
//    def entity: ForeignKeyQuery[Entities, Entity] =
//      foreignKey("GRAPH_DOMAINS_FK_ENTITIES", id, entities)(_.id)

    // *
    def * : ProvenShape[GraphDomain] = (id, namespace) <> (GraphDomain.tupled, GraphDomain.unapply)

  }

  object graphDomains extends TableQuery(new GraphDomains(_)) with AbstractEntitiesTableQuery[GraphDomain, GraphDomains] {

    val findByNamespace: CompiledFunction[Rep[String] => Query[GraphDomains, GraphDomain, Seq], Rep[String], String, Query[GraphDomains, GraphDomain, Seq], Seq[GraphDomain]] =
      this.findBy(_.namespace)

  }

  _schemas += graphDomains.schema

}
