package models.persistence

import java.time.Instant
import java.util.UUID

import models.{ Event, FileObject }
import play.api.libs.json.{ JsObject, JsString, Json, OFormat }
import slick.lifted._

import scala.concurrent.ExecutionContext.Implicits.global

trait FileObjectComponent {

  this: JdbcProfileComponent with SchemasComponent with ImplicitsComponent with EventComponent =>

  import profile.api._

  implicit lazy val FileObjectFormat: OFormat[FileObject] = FileObject.format

  class FileObjects( tag: Tag ) extends Table[FileObject]( tag, "FILEOBJECTS" ) {

    // Columns
    def uuid: Rep[UUID] = column[UUID]( "UUID", O.PrimaryKey )

    def name: Rep[String] = column[String]( "NAME" )

    def description: Rep[String] = column[String]( "DESCRIPTION" )

    def hash: Rep[String] = column[String]( "HASH" )

    def created: Rep[Instant] = column[Instant]( "CREATED" )

    def owner: Rep[UUID] = column[UUID]( "OWNER" )

    // Indexes
    def idx: Index = index( "IDX_FILEOBJECTS_OWNER", owner, unique = false )

    // *
    def * : ProvenShape[FileObject] = ( uuid, name, description, hash, created.?, owner ) <> ( ( FileObject.apply _ ).tupled, FileObject.unapply )

  }

  object fileObjects extends TableQuery( new FileObjects( _ ) ) {
    def findByUUID( id: UUID ): DBIO[Option[FileObject]] = {
      fileObjects.findBy( _.uuid ).extract( id ).result.headOption
    }
    def findByHash( id: String ): DBIO[Option[FileObject]] = {
      fileObjects.findBy( _.hash ).extract( id ).result.headOption
    }
    def all(): DBIO[Seq[FileObject]] = {
      fileObjects.result
    }
    def insert( r: FileObject ): DBIO[Int] = {
      ( for {
        ins <- fileObjects += r
        _ <- events.insert( Event( None, JsObject( Map( "uuid" -> JsString( r.uuid.toString ) ) ), "insert", Json.toJson( r ), Instant.now() ) )
      } yield ins ).transactionally
    }
    def update( r: FileObject ): DBIO[Int] = {
      ( for {
        up <- ( for { c <- fileObjects if c.uuid === r.uuid } yield c ).update( r )
        _ <- events.insert( Event( None, JsObject( Map( "uuid" -> JsString( r.uuid.toString ) ) ), "update", Json.toJson( r ), Instant.now() ) )
      } yield up ).transactionally
    }
  }

  _schemas += fileObjects.schema

}
