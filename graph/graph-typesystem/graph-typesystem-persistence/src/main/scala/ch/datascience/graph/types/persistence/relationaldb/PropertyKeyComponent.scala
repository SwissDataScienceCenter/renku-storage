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

package ch.datascience.graph.types.persistence.relationaldb

import java.util.UUID

import ch.datascience.graph.types.persistence.model.{GraphDomain, PropertyKey, RichPropertyKey}
import ch.datascience.graph.types.{Cardinality, DataType}
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

//    def mappedUsing(graphDomains: GraphDomains): MappedProjection[PropertyKey, (UUID, GraphDomain, String, DataType, Cardinality)] = {
//      (id, graphDomains.mapped, name, dataType, cardinality).mapTo[PropertyKey]
//    }

  }

  final class RichPropertyKeysQuery[C[_]](self: Query[PropertyKeys, PropertyKey, C]) {

    def withGraphDomain: Query[(GraphDomains, PropertyKeys), (GraphDomain, PropertyKey), C] = for {
      pk <- self
      gd <- pk.graphDomain
    } yield (gd, pk)

    def mapped: Query[MappedProjection[RichPropertyKey, (GraphDomain, PropertyKey)], RichPropertyKey, C] = for {
      (gd, pk) <- this.withGraphDomain
    } yield (gd, pk) <> (to, from)

    protected def to(t: (GraphDomain, PropertyKey)): RichPropertyKey = t._2.toRichPropertyKey(t._1)

    protected def from(propertyKey: RichPropertyKey): Option[(GraphDomain, PropertyKey)] = RichPropertyKey.unapply(propertyKey) match {
      case Some((_, gd, _, _, _)) => Some((gd, propertyKey))
      case _ => None
    }

//    def mapped: Query[MappedProjection[PropertyKey, (UUID, GraphDomain, String, DataType, Cardinality)], PropertyKey, C] = for {
//      (gd, pk) <- self.withGraphDomain
//    } yield pk.mappedUsing(gd)

  }

  implicit def toRichPropertyKeysQuery[C[_]](query: Query[PropertyKeys, PropertyKey, C]): RichPropertyKeysQuery[C] = new RichPropertyKeysQuery(query)

  object propertyKeys extends TableQuery(new PropertyKeys(_)) with AbstractEntitiesTableQuery[PropertyKey, RichPropertyKey, PropertyKeys] {

//    type MappedQuery = Query[MappedProjection[PropertyKey, (UUID, GraphDomain, String, DataType, Cardinality)], PropertyKey, Seq]
//
//    lazy val findById: CompiledFunction[Rep[UUID] => MappedQuery, Rep[UUID], UUID, MappedQuery, Seq[PropertyKey]] = Compiled {
//      this.findById.extract andThen {_.mapped }
//    }
//
//    lazy val findRowByNamespaceAndName: CompiledFunction[(Rep[String], Rep[String]) => Query[PropertyKeys, PropertyKey, Seq], (Rep[String], Rep[String]), (String, String), Query[PropertyKeys, PropertyKey, Seq], Seq[PropertyKey]] = Compiled {
//      (namespace: Rep[String], name: Rep[String]) => for {
//        (gd, pk) <- this.withGraphDomain
//        if gd.namespace === namespace && pk.name === name
//      } yield pk
//    }
//
//    lazy val findByNamespaceAndName: CompiledFunction[(Rep[String], Rep[String]) => MappedQuery, (Rep[String], Rep[String]), (String, String), MappedQuery, Seq[PropertyKey]] = Compiled {
//      (namespace: Rep[String], name: Rep[String]) => for {
//        (gd, pk) <- this.withGraphDomain
//        if gd.namespace === namespace && pk.name === name
//      } yield pk.mappedUsing(gd)
//    }

//    override lazy val findById: CompiledFunction[(profile.api.Rep[UUID]) => profile.api.Query[PropertyKeys, PropertyKey, Seq], profile.api.Rep[UUID], UUID, profile.api.Query[PropertyKeys, PropertyKey, Seq], Seq[PropertyKey]] = _

    lazy val findById: CompiledFunction[Rep[UUID] => Query[MappedProjection[RichPropertyKey, (GraphDomain, PropertyKey)], RichPropertyKey, Seq], Rep[UUID], UUID, Query[MappedProjection[RichPropertyKey, (GraphDomain, PropertyKey)], RichPropertyKey, Seq], Seq[RichPropertyKey]] = Compiled {
      this.findBy(_.id).extract andThen { _.mapped }
    }

    lazy val findByNamespaceAndName: CompiledFunction[(Rep[String], Rep[String]) => Query[MappedProjection[RichPropertyKey, (GraphDomain, PropertyKey)], RichPropertyKey, Seq], (Rep[String], Rep[String]), (String, String), Query[MappedProjection[RichPropertyKey, (GraphDomain, PropertyKey)], RichPropertyKey, Seq], Seq[RichPropertyKey]] = Compiled {
      (namespace: Rep[String], name: Rep[String]) => (for {
        (gd, pk) <- this.withGraphDomain
        if gd.namespace === namespace && pk.name === name
      } yield pk).mapped
    }

  }

  _schemas += propertyKeys.schema

}
