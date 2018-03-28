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

    private val compiledListByRepository = Compiled( ( id: Rep[UUID] ) =>
      for {
        fo <- fileObjects
        fr <- fileObjectRepositories
        if fr.fileObject === fo.uuid && fr.repository === id
      } yield ( fr, fo ) )
    def listByRepository( id: UUID ): DBIO[Seq[( FileObjectRepository, FileObject )]] = compiledListByRepository( id ).result

    private val compiledListByFileObject = Compiled( ( id: Rep[UUID] ) =>
      for {
        fr <- fileObjectRepositories
        re <- repositories
        if fr.repository === re.uuid && fr.fileObject === id
      } yield ( fr, re ) )
    def listByFileObject( id: UUID ): DBIO[Seq[( FileObjectRepository, Repository )]] = compiledListByFileObject( id ).result

    private val compiledListByFileObjectHash = Compiled( ( hash: Rep[String] ) =>
      for {
        fo <- fileObjects
        fr <- fileObjectRepositories
        re <- repositories
        if fr.fileObject === fo.uuid && fr.repository === re.uuid && fo.hash === hash
      } yield ( re, fr, fo ) )
    def listByFileObjectHash( hash: String ): DBIO[Seq[( Repository, FileObjectRepository, FileObject )]] = compiledListByFileObjectHash( hash ).result

    private val compiledFindByPk = Compiled( ( rid: Rep[UUID], oid: Rep[UUID] ) =>
      for {
        fo <- fileObjects
        fr <- fileObjectRepositories
        re <- repositories
        if fr.fileObject === fo.uuid && fr.repository === re.uuid && fo.uuid === oid && re.uuid === rid
      } yield ( re, fr, fo ) )
    def findByPk( rid: UUID, oid: UUID ): DBIO[Seq[( Repository, FileObjectRepository, FileObject )]] = compiledFindByPk( rid, oid ).result

    def all(): DBIO[Seq[FileObjectRepository]] = {
      fileObjectRepositories.result
    }

    def insert( r: FileObjectRepository ): DBIO[Int] = {
      fileObjectRepositories += r
    }
  }

  _schemas += fileObjectRepositories.schema

}
