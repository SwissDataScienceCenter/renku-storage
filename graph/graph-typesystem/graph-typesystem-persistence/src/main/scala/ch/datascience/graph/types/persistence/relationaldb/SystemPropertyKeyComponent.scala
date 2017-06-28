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

import ch.datascience.graph.types.persistence.model.SystemPropertyKey
import ch.datascience.graph.types.{Cardinality, DataType}
import slick.lifted._

/**
  * Created by johann on 17/03/17.
  */
trait SystemPropertyKeyComponent { this: JdbcProfileComponent with SchemasComponent with ImplicitsComponent with EntityComponent with AbstractEntityComponent =>

  import profile.api._

  class SystemPropertyKeys(tag: Tag) extends Table[SystemPropertyKey](tag, "SYSTEM_PROPERTY_KEYS") with AbstractEntityTable[SystemPropertyKey] {

    // Columns
//    def id: Rep[UUID] = column[UUID]("UUID", O.PrimaryKey)

    def name: Rep[String] = column[String]("NAME")

    def dataType: Rep[DataType] = column[DataType]("DATA_TYPE")

    def cardinality: Rep[Cardinality] = column[Cardinality]("CARDINALITY")

    // Indexes
    def idx: Index = index("IDX_SYSTEM_PROPERTY_KEYS_NAME", name, unique = true)

    // Foreign keys
//    def entity: ForeignKeyQuery[Entities, Entity] =
//      foreignKey("PROPERTY_KEYS_FK_ENTITIES", id, entities)(_.id)

    // *
    def * : ProvenShape[SystemPropertyKey] =
      (id, name, dataType, cardinality) <> (SystemPropertyKey.tupled, SystemPropertyKey.unapply)

  }

  object systemPropertyKeys extends TableQuery(new SystemPropertyKeys(_)) with AbstractEntitiesTableQuery[SystemPropertyKey, SystemPropertyKey, SystemPropertyKeys] {

    lazy val findById: CompiledFunction[Rep[UUID] => Query[SystemPropertyKeys, SystemPropertyKey, Seq], Rep[UUID], UUID, Query[SystemPropertyKeys, SystemPropertyKey, Seq], Seq[SystemPropertyKey]] = {
      this.findBy(_.id)
    }

    lazy val findByName: CompiledFunction[Rep[String] => Query[SystemPropertyKeys, SystemPropertyKey, Seq], Rep[String], String, Query[SystemPropertyKeys, SystemPropertyKey, Seq], Seq[SystemPropertyKey]] = {
      this.findBy(_.name)
    }

  }

  _schemas += systemPropertyKeys.schema

}
