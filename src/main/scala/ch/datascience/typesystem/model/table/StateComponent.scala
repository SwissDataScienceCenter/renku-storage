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

import java.time.Instant
import java.util.UUID

import ch.datascience.typesystem.model.EntityState
import ch.datascience.typesystem.model.row.{Entity, State}
import slick.jdbc.JdbcProfile
import slick.lifted.{CompiledFunction, ForeignKeyQuery, Index, ProvenShape}

/**
  * Created by johann on 20/03/17.
  */
trait StateComponent { this: JdbcProfileComponent with SchemasComponent with ImplicitsComponent with EntityComponent =>

  import profile.api._

  class States(tag: Tag) extends Table[State](tag, "STATES") {

    // Columns
    def id: Rep[Long] = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def entityId: Rep[UUID] = column[UUID]("ENTITY_UUID")

    def state: Rep[EntityState] = column[EntityState]("STATE")

    def timestamp: Rep[Instant] = column[Instant]("STATE_TIMESTAMP")

    // Indexes
    def entityIdIdx: Index = index("IDX_STATES_ENTITY_UUID", entityId)

    // Foreign keys
    def entity: ForeignKeyQuery[Entities, Entity] =
      foreignKey("STATES_FK_ENTITIES", entityId, entities)(_.id)

    // *
    def * : ProvenShape[State] = (id.?, entityId, state, timestamp) <> (State.tupled, State.unapply)

  }

  object states extends TableQuery(new States(_)) {

    val findByEntityId: CompiledFunction[Rep[UUID] => Query[States, State, Seq], Rep[UUID], UUID, Query[States, State, Seq], Seq[State]] =
      this.findBy(_.entityId)

  }

  _schemas += states.schema

}
