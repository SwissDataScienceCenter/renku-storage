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

  object fileobjects extends TableQuery( new FileObjects( _ ) ) {
    def findByUUID( id: UUID ): Future[Option[FileObject]] = {
      db.run( fileobjects.findBy( _.uuid ).extract( id ).result ).map( s => s.headOption.map( r => r.copy() ) )
    }
    def findByHash( id: String ): Future[Option[FileObject]] = {
      db.run( fileobjects.findBy( _.hash ).extract( id ).result ).map( s => s.headOption.map( r => r.copy() ) )
    }
    def all(): Future[Seq[FileObject]] = {

      db.run( fileobjects.result )
    }
    def insert( r: FileObject ): Future[Int] = {
      db.run( fileobjects += r )
    }
    def update( r: FileObject ): Future[Int] = {
      db.run( ( for { c <- fileobjects if c.uuid === r.uuid } yield c ).update( r ) )
    }
  }

  _schemas += fileobjects.schema

}
