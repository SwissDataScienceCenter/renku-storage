package models.persistence

import java.time.Instant
import java.util.UUID

import models.FileObjectRepository
import slick.lifted._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait FileObjectRepositoryComponent {

  this: JdbcProfileComponent with SchemasComponent with ImplicitsComponent with RepositoryComponent with FileObjectComponent =>

  import profile.api._

  class FileObjectRepositories( tag: Tag ) extends Table[FileObjectRepository]( tag, "FILEOBJECTREPOSITORIES" ) {

    // Columns
    def fileObject: Rep[UUID] = column[UUID]( "FILEOBJECT" )

    def repository: Rep[UUID] = column[UUID]( "REPOSITORY" )

    def iid: Rep[String] = column[String]( "IID" )

    def created: Rep[Instant] = column[Instant]( "CREATED" )

    // Indexes
    def pk: PrimaryKey = primaryKey( "PK_FILEOBJECTREPOSITORIES", ( fileObject, repository ) )
    def fk0 = foreignKey( "FK_FILEOBJECTREPOSITORIES_REPOSITORY", repository, repositories )( _.uuid, onDelete = ForeignKeyAction.Cascade )
    def fk1 = foreignKey( "FK_FILEOBJECTREPOSITORIES_FILEOBJECT", fileObject, fileobjects )( _.uuid, onDelete = ForeignKeyAction.Cascade )

    // *
    def * : ProvenShape[FileObjectRepository] = ( fileObject, repository, iid.?, created.? ) <> ( ( FileObjectRepository.apply _ ).tupled, FileObjectRepository.unapply )

  }

  object fileobjectrepositories extends TableQuery( new FileObjectRepositories( _ ) ) {
    def listByRepository( id: UUID ) = {
      val query = for {
        ( fileObjectRepository, fileObject ) <- fileobjectrepositories join fileobjects on ( _.fileObject === _.uuid )
        if fileObjectRepository.repository === id
      } yield ( fileObjectRepository, fileObject )
      db.run( query.result )
    }
    def listByFileObject( id: UUID ) = {
      val query = for {
        ( fileObjectRepository, repository ) <- fileobjectrepositories join repositories on ( _.repository === _.uuid )
        if fileObjectRepository.fileObject === id
      } yield ( fileObjectRepository, repository )
      db.run( query.result )
    }
    def findByPk( rid: UUID, oid: UUID ) = {
      val query = for {
        ( fileObjects, repository ) <- repositories join ( fileobjectrepositories join fileobjects on ( _.fileObject === _.uuid ) ) on ( _.uuid === _._1.repository )
        if repository._1.repository === rid && fileObjects.uuid === oid
      } yield ( fileObjects, repository )
      db.run( query.result )
    }
    def all(): Future[Seq[FileObjectRepository]] = {
      db.run( fileobjectrepositories.result )
    }
    def insert( r: FileObjectRepository ): Future[Int] = {
      db.run( fileobjectrepositories += r )
    }
  }

  _schemas += fileobjectrepositories.schema

}
