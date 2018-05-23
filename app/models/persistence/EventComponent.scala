package models.persistence

import java.time.Instant
import java.util.UUID
import play.api.libs.json.JsValue

import models.Event
import slick.lifted._
import scala.language.{ higherKinds, implicitConversions }

trait EventComponent {

  this: JdbcProfileComponent with SchemasComponent with ImplicitsComponent =>

  import profile.api._

  class Events( tag: Tag ) extends Table[Event]( tag, "EVENTS" ) {

    // Columns
    def id: Rep[Long] = column[Long]( "ID", O.PrimaryKey, O.AutoInc )

    def obj: Rep[JsValue] = column[JsValue]( "OBJ" )

    def action: Rep[String] = column[String]( "ACTION" )

    def attr: Rep[JsValue] = column[JsValue]( "ATTR" )

    def created: Rep[Instant] = column[Instant]( "CREATED" )

    // *
    def * : ProvenShape[Event] = ( id, obj, action, attr, created ) <> ( ( Event.apply _ ).tupled, Event.unapply _ )

  }

  object events extends TableQuery( new Events( _ ) ) {
    def findByID( id: Long ): DBIO[Option[Event]] = {
      events.findBy( _.id ).extract( id ).result.headOption
    }
    def all(): DBIO[Seq[Event]] = {
      events.result
    }
    def insert( e: Event ): DBIO[Int] = {
      events += e
    }
  }

  _schemas += events.schema

}
