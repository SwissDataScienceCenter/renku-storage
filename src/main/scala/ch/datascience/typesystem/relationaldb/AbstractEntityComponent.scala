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

package ch.datascience.typesystem.relationaldb

import java.time.Instant
import java.util.UUID

import ch.datascience.typesystem.model.EntityState
import ch.datascience.typesystem.model.relational.AbstractEntity
import ch.datascience.typesystem.relationaldb.row.{Entity, State}
import slick.lifted.{CompiledFunction, ForeignKeyQuery}

/**
  * Created by johann on 20/03/17.
  */
trait AbstractEntityComponent { this: JdbcProfileComponent with EntityComponent with StateComponent =>

  import profile.api._

  trait AbstractEntityTable[A <: AbstractEntity] extends Table[A] {

    // Columns
    def id: Rep[UUID] = column[UUID]("UUID", O.PrimaryKey)

    // Foreign keys
    def entity: ForeignKeyQuery[Entities, Entity] =
      foreignKey(s"${this.tableName}_FK_ENTITIES", id, entities)(_.id)

  }

  trait AbstractEntitiesTableQuery[A <: AbstractEntity, TA <: Table[A] with AbstractEntityTable[A]] { this: TableQuery[TA] =>

    val findById: CompiledFunction[Rep[UUID] => Query[TA, A, Seq], Rep[UUID], UUID, Query[TA, A, Seq], Seq[A]] =
      Compiled { (entityId: Rep[UUID]) => this.filter(_.id === entityId) }

    def add(entity: A): DBIO[Int] = {
      val insertEntity = entities += Entity(entity.id, entity.entityType)
      val insertState = states += State(None, entity.id, EntityState.Pending, Instant.now())
      val insertConcrete = this += entity
      (insertEntity andThen insertState andThen insertConcrete).transactionally
    }

  }

}
