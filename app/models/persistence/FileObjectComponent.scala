package models.persistence

import java.time.Instant
import java.util.UUID

import models.FileObject
import slick.lifted._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait FileObjectComponent {

  this: JdbcProfileComponent with SchemasComponent with ImplicitsComponent =>

  import profile.api._

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
      fileObjects += r
    }
    def update( r: FileObject ): DBIO[Int] = {
      ( for { c <- fileObjects if c.uuid === r.uuid } yield c ).update( r )
    }
  }

  _schemas += fileObjects.schema

}
