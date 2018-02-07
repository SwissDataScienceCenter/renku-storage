package models.persistence

import java.time.Instant
import java.util.UUID

import ch.datascience.graph.types.persistence.relationaldb._
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

    // Indexes
    def idx: Index = index( "IDX_REPOSITORIES_IID_BACKEND", ( iid, backend ), unique = true )

    // *
    def * : ProvenShape[Repository] = ( uuid, iid.?, path, description, backend, created.? ) <> ( ( Repository.apply _ ).tupled, Repository.unapply )

  }

  object repositories extends TableQuery( new Repositories( _ ) ) {
    def findByUUID( id: UUID ): Future[Option[Repository]] = {
      db.run( repositories.findBy( _.uuid ).extract( id ).result ).map( s => s.headOption.map( r => r.copy() ) )
    }
    def findByIID( id: String ): Future[Option[Repository]] = {
      db.run( repositories.findBy( _.iid ).extract( id ).result ).map( s => s.headOption.map( r => r.copy() ) )
    }
    def all(): Future[Seq[Repository]] = {

      db.run( repositories.result )
    }
    def insert( r: Repository ): Future[Int] = {
      db.run( repositories += r )
    }
    def update( r: Repository ): Future[Int] = {
      db.run( ( for { c <- repositories if c.uuid === r.uuid } yield c ).update( r ) )
    }
  }

  _schemas += repositories.schema

}
