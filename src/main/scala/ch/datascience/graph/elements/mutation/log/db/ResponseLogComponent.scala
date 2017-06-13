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

package ch.datascience.graph.elements.mutation.log.db

import java.time.Instant
import java.util.UUID

import ch.datascience.graph.elements.mutation.log.model.Event
import play.api.libs.json.JsValue
import slick.lifted.{CompiledFunction, ForeignKeyQuery, ProvenShape}

import scala.concurrent.ExecutionContext

/**
  * Created by johann on 07/06/17.
  */
trait ResponseLogComponent { this: JdbcProfileComponent with ImplicitsComponent with RequestLogComponent =>

  import profile.api._

  class Responses(tag: Tag) extends Table[Event](tag, "RESPONSE_LOG") {

    def id: Rep[UUID] = column[UUID]("UUID", O.PrimaryKey)

    def event: Rep[JsValue] = column[JsValue]("EVENT")

    def created: Rep[Instant] = column[Instant]("CREATED")

    def * : ProvenShape[Event] = (id, event, created) <> (Event.tupled, Event.unapply)

    // Foreign keys
    def request: ForeignKeyQuery[Requests, Event] = foreignKey(s"RESPONSE_FK_REQUEST", id, requests)(_.id)

  }

  object responses extends TableQuery(new Responses(_)) {

    lazy val findById: CompiledFunction[Rep[UUID] => Query[Responses, Event, Seq], Rep[UUID], UUID, Query[Responses, Event, Seq], Seq[Event]] = {
      this.findBy(_.id)
    }

    def add(requestId: UUID, event: JsValue): DBIO[Event] = insert(requestId, event) andThen {
      this.filter(_.id === requestId).result.head
    }

    private[this] def insert(requestId: UUID, event: JsValue): DBIO[Int] = {
      val query = for { event <- this } yield (event.id, event.event)

      query += (requestId, event)
    }

  }

}
