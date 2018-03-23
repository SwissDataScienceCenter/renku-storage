package models.persistence

import java.time.Instant
import java.util.UUID

import models.{ FileObject, FileObjectRepository, Repository }
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
    def fk1 = foreignKey( "FK_FILEOBJECTREPOSITORIES_FILEOBJECT", fileObject, fileObjects )( _.uuid, onDelete = ForeignKeyAction.Cascade )

    // *
    def * : ProvenShape[FileObjectRepository] = ( fileObject, repository, iid.?, created.? ) <> ( ( FileObjectRepository.apply _ ).tupled, FileObjectRepository.unapply )

  }

  object fileObjectRepositories extends TableQuery( new FileObjectRepositories( _ ) ) {
    def listByRepository( id: UUID ): DBIO[Seq[( FileObjectRepository, FileObject )]] = {
      val query = for {
        ( fileObjectRepository, fileObject ) <- fileObjectRepositories join fileObjects on ( _.fileObject === _.uuid )
        if fileObjectRepository.repository === id
      } yield ( fileObjectRepository, fileObject )
      query.result
    }
    def listByFileObject( id: UUID ): DBIO[Seq[( FileObjectRepository, Repository )]] = {
      val query = for {
        ( fileObjectRepository, repository ) <- fileObjectRepositories join repositories on ( _.repository === _.uuid )
        if fileObjectRepository.fileObject === id
      } yield ( fileObjectRepository, repository )
      query.result
    }
    def listByFileObjectHash( hash: String ): DBIO[Seq[( Repository, ( FileObjectRepository, FileObject ) )]] = {
      val query = for {
        ( repository, fileObjects ) <- repositories join ( fileObjectRepositories join fileObjects on ( _.fileObject === _.uuid ) ) on ( _.uuid === _._1.repository )
        if fileObjects._2.hash === hash
      } yield ( repository, fileObjects )
      query.result
    }
    def findByPk( rid: UUID, oid: UUID ): DBIO[Seq[( Repository, ( FileObjectRepository, FileObject ) )]] = {
      val query = for {
        ( fileObjects, repository ) <- repositories join ( fileObjectRepositories join fileObjects on ( _.fileObject === _.uuid ) ) on ( _.uuid === _._1.repository )
        if repository._1.repository === rid && fileObjects.uuid === oid
      } yield ( fileObjects, repository )
      query.result
    }
    def all(): DBIO[Seq[FileObjectRepository]] = {
      fileObjectRepositories.result
    }
    def insert( r: FileObjectRepository ): DBIO[Int] = {
      fileObjectRepositories += r
    }
  }

  _schemas += fileObjectRepositories.schema

}
