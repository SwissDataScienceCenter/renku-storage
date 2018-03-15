package models.persistence

import java.time.Instant
import java.util.UUID

import models.Repository
import slick.lifted._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait RepositoryComponent {

  this: JdbcProfileComponent with SchemasComponent with ImplicitsComponent =>

  import profile.api._

  class Repositories( tag: Tag ) extends Table[Repository]( tag, "REPOSITORIES" ) {

    // Columns
    def uuid: Rep[UUID] = column[UUID]( "UUID", O.PrimaryKey )

    def iid: Rep[String] = column[String]( "IID" )

    def path: Rep[String] = column[String]( "PATH" )

    def description: Rep[String] = column[String]( "DESCRIPTION" )

    def backend: Rep[String] = column[String]( "BACKEND" )

    def created: Rep[Instant] = column[Instant]( "CREATED" )

    def owner: Rep[UUID] = column[UUID]( "OWNER" )

    def lfs_store: Rep[Option[UUID]] = column[Option[UUID]]( "LFS_STORE" )

    // Indexes
    def idx0: Index = index( "IDX_REPOSITORIES_IID_BACKEND", ( iid, backend ), unique = true )
    def idx1: Index = index( "IDX_REPOSITORIES_OWNER", owner, unique = false )

    // *
    def * : ProvenShape[Repository] = ( uuid, iid.?, path, description, backend, created.?, owner.?, lfs_store ) <> ( ( Repository.apply _ ).tupled, Repository.unapply )

  }

  object repositories extends TableQuery( new Repositories( _ ) ) {
    def findByUUID( id: UUID ): DBIO[Option[Repository]] = {
      repositories.findBy( _.uuid ).extract( id ).result.headOption
    }
    def findByIID( id: String ): DBIO[Option[Repository]] = {
      repositories.findBy( _.iid ).extract( id ).result.headOption
    }
    def all(): DBIO[Seq[Repository]] = {
      repositories.result
    }
    def insert( r: Repository ): DBIO[Int] = {
      repositories += r
    }
    def update( r: Repository ): DBIO[Int] = {
      ( for { c <- repositories if c.uuid === r.uuid } yield c ).update( r )
    }
  }

  _schemas += repositories.schema

}
