package models.persistence

import java.time.Instant
import java.util.UUID

import models.{ Event, FileObject, FileObjectRepository, Repository }
import play.api.libs.json.{ JsObject, JsString, Json, OFormat }
import slick.lifted._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait FileObjectRepositoryComponent {

  this: JdbcProfileComponent with SchemasComponent with ImplicitsComponent with RepositoryComponent with FileObjectComponent with EventComponent =>

  import profile.api._

  implicit lazy val FileObjectRepositoryFormat: OFormat[FileObjectRepository] = FileObjectRepository.format

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

    /*private val compiledAll = Compiled(
      for {
        fo <- fileObjects
        fr <- fileObjectRepositories
        re <- repositories
        if fr.fileObject === fo.uuid && fr.repository === re.uuid
      } yield ( re, fr, fo )
    )

    def all(): DBIO[Seq[( Repository, FileObjectRepository, FileObject )]] = compiledAll.result
    */

    def all(): DBIO[Seq[( Repository, FileObjectRepository, FileObject )]] = {
      val query = for {
        fo <- fileObjects
        fr <- fileObjectRepositories
        re <- repositories
        if fr.fileObject === fo.uuid && fr.repository === re.uuid
      } yield ( re, fr, fo )
      query.result
    }

    def insert( r: FileObjectRepository ): DBIO[Int] = {
      ( for {
        ins <- fileObjectRepositories += r
        log <- events += Event( 0, JsObject( Map( "repository" -> JsString( r.repository.toString ), "fileObject" -> JsString( r.fileObject.toString ) ) ), "insert", Json.toJson( r ), Instant.now() )
      } yield ins ).transactionally

    }
  }

  _schemas += fileObjectRepositories.schema

}
