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
import slick.lifted.{CompiledFunction, ProvenShape}

import scala.concurrent.ExecutionContext
import scala.language.{higherKinds, implicitConversions}

/**
  * Created by johann on 07/06/17.
  */
trait RequestLogComponent { this: JdbcProfileComponent with ImplicitsComponent with ResponseLogComponent =>

  import profile.api._

  class Requests(tag: Tag) extends Table[Event](tag, "REQUEST_LOG") {

    def id: Rep[UUID] = column[UUID]("UUID", O.PrimaryKey)

    def event: Rep[JsValue] = column[JsValue]("EVENT")

    def created: Rep[Instant] = column[Instant]("CREATED")

    def * : ProvenShape[Event] = (id, event, created) <> (Event.tupled, Event.unapply)

  }

  final class RichRequestsQuery[C[T] <: Seq[T]](self: Query[Requests, Event, C]) {
    def withResponse: Query[(Requests, Rep[Option[Responses]]), (Event, Option[Event]), C] = for {
      (request, optResponse) <- self.joinLeft(responses) on (_.id === _.id)
    } yield (request, optResponse)
  }

  implicit def toRichRequestsQuery[C[T] <: Seq[T]](query: Query[Requests, Event, C]): RichRequestsQuery[C] = new RichRequestsQuery(query)

  object requests extends TableQuery(new Requests(_)) {

    lazy val findById: CompiledFunction[Rep[UUID] => Query[Requests, Event, Seq], Rep[UUID], UUID, Query[Requests, Event, Seq], Seq[Event]] = {
      this.findBy(_.id)
    }

    lazy val findByIdWithResponse: CompiledFunction[Rep[UUID] => Query[(Requests, Rep[Option[Responses]]), (Event, Option[Event]), Seq], Rep[UUID], UUID, Query[(Requests, Rep[Option[Responses]]), (Event, Option[Event]), Seq], Seq[(Event, Option[Event])]] = Compiled {
      this.findBy(_.id).extract.andThen(_.withResponse)
    }

    def add(event: JsValue)(implicit ec: ExecutionContext): DBIO[Event] = insert(event).flatMap { id =>
      this.filter(_.id === id).result.head
    }

    private[this] def insert(event: JsValue): DBIO[UUID] = {
      val query = (for { event <- this } yield event.event) returning this.map(_.id)

      query += event
    }

  }

}
