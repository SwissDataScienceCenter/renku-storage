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
package relationaldb

import java.util.UUID

import ch.datascience.typesystem.model.relational.{GraphDomain, PropertyKey}
import ch.datascience.typesystem.model.{Cardinality, DataType, PropertyKey => ModelPropertyKey}
import slick.lifted._

import scala.language.{higherKinds, implicitConversions}

/**
  * Created by johann on 17/03/17.
  */
trait PropertyKeyComponent { this: JdbcProfileComponent with SchemasComponent with ImplicitsComponent with EntityComponent with GraphDomainComponent with AbstractEntityComponent =>

  import profile.api._

  class PropertyKeys(tag: Tag) extends Table[PropertyKey](tag, "PROPERTY_KEYS") with AbstractEntityTable[PropertyKey] {

    // Columns
//    def id: Rep[UUID] = column[UUID]("UUID", O.PrimaryKey)

    def graphDomainId: Rep[UUID] = column[UUID]("GRAPH_DOMAIN_ID")

    def name: Rep[String] = column[String]("NAME")

    def dataType: Rep[DataType] = column[DataType]("DATA_TYPE")

    def cardinality: Rep[Cardinality] = column[Cardinality]("CARDINALITY")

    // Indexes
    def idx: Index = index("IDX_PROPERTY_KEYS_GRAPH_DOMAIN_ID_NAME", (graphDomainId, name), unique = true)

    // Foreign keys
//    def entity: ForeignKeyQuery[Entities, Entity] =
//      foreignKey("PROPERTY_KEYS_FK_ENTITIES", id, entities)(_.id)

    def graphDomain: ForeignKeyQuery[GraphDomains, GraphDomain] =
      foreignKey("PROPERTY_KEYS_FK_GRAPH_DOMAINS", graphDomainId, graphDomains)(_.id)

    // *
    def * : ProvenShape[PropertyKey] =
      (id, graphDomainId, name, dataType, cardinality) <> (PropertyKey.tupled, PropertyKey.unapply)

  }

  final class RichPropertyKeysQuery[C[_]](self: Query[PropertyKeys, PropertyKey, C]) {

    def withGraphDomain: Query[(GraphDomains, PropertyKeys), (GraphDomain, PropertyKey), C] = for {
      pk <- self
      gd <- pk.graphDomain
    } yield (gd, pk)

    def mapped: Query[MappedProjection[ModelPropertyKey, (UUID, GraphDomain, String, DataType, Cardinality)], ModelPropertyKey, C] = for {
      (gd, pk) <- self.withGraphDomain
      tuple = (pk.id, gd, pk.name, pk.dataType, pk.cardinality)
    } yield tuple.mapTo[ModelPropertyKey]

  }

  implicit def toRichPropertyKeysQuery[C[_]](query: Query[PropertyKeys, PropertyKey, C]): RichPropertyKeysQuery[C] = new RichPropertyKeysQuery(query)

  object propertyKeys extends TableQuery(new PropertyKeys(_)) with AbstractEntitiesTableQuery[PropertyKey, PropertyKeys] {

    lazy val findByIdAsModel = Compiled {
      (entityId: Rep[UUID]) => (for {
        pk <- this.filter(_.id === entityId)
      } yield pk).mapped
    }

    lazy val findByNamespaceAndName: CompiledFunction[(Rep[String], Rep[String]) => Query[PropertyKeys, PropertyKey, Seq], (Rep[String], Rep[String]), (String, String), Query[PropertyKeys, PropertyKey, Seq], Seq[PropertyKey]] = Compiled {
      (namespace: Rep[String], name: Rep[String]) => for {
        (gd, pk) <- this.withGraphDomain
        if gd.namespace === namespace && pk.name === name
      } yield pk
    }

    lazy val findByNamespaceAndNameAsModel = Compiled {
      (namespace: Rep[String], name: Rep[String]) => (for {
        (gd, pk) <- this.withGraphDomain
        if gd.namespace === namespace && pk.name === name
      } yield pk).mapped
    }

  }

  _schemas += propertyKeys.schema

}
