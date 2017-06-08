package ch.datascience.graph.elements.mutation.log.db

import java.time.Instant
import java.util.UUID

import ch.datascience.graph.elements.mutation.log.model.Event
import play.api.libs.json.JsValue
import slick.lifted.{CompiledFunction, ProvenShape}

import scala.concurrent.ExecutionContext

/**
  * Created by johann on 07/06/17.
  */
trait RequestLogComponent { this: JdbcProfileComponent with ImplicitsComponent =>

  import profile.api._

  class Requests(tag: Tag) extends Table[Event](tag, "REQUEST_LOG") {

    def id: Rep[UUID] = column[UUID]("UUID", O.PrimaryKey)

    def event: Rep[JsValue] = column[JsValue]("EVENT")

    def created: Rep[Instant] = column[Instant]("CREATED")

    def * : ProvenShape[Event] = (id, event, created) <> (Event.tupled, Event.unapply)

  }

  object requests extends TableQuery(new Requests(_)) {

    lazy val findById: CompiledFunction[Rep[UUID] => Query[Requests, Event, Seq], Rep[UUID], UUID, Query[Requests, Event, Seq], Seq[Event]] = {
      this.findBy(_.id)
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
