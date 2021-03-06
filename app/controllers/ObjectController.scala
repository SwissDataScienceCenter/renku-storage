/*
 * Copyright 2017 - Swiss Data Science Center (SDSC)
 * A partnership between École Polytechnique Fédérale de Lausanne (EPFL) and
 * Eidgenössische Technische Hochschule Zürich (ETHZ).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

import authorization.JWTVerifierProvider
import ch.datascience.service.security.{ TokenFilter, TokenFilterActionBuilder }
import ch.datascience.service.utils.ControllerWithBodyParseJson
import controllers.storageBackends.{ Backends, ObjectBackend }
import javax.inject.{ Inject, Singleton }
import models.persistence.DatabaseLayer
import models.{ FileObject, FileObjectRepository, Repository }
import play.api.Logger
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json._
import play.api.libs.streams.Accumulator
import play.api.mvc._
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }

/**
 * Created by jeberle on 25.04.17.
 */
@Singleton
class ObjectController @Inject() (
    config:            play.api.Configuration,
    jwtVerifier:       JWTVerifierProvider,
    tokenFilterAction: TokenFilterActionBuilder,
    backends:          Backends,
    protected val dal: DatabaseLayer,
    cc:                ControllerComponents
) extends AbstractController( cc ) with ControllerWithBodyParseJson with HasDatabaseConfig[JdbcProfile] {

  override protected val dbConfig = dal.dbConfig
  import profile.api._

  lazy val logger: Logger = Logger( "application.AuthorizeController" )
  val default_backend: String = config.get[String]( "lfs_default_backend" )

  implicit lazy val FileObjectFormat: OFormat[FileObject] = FileObject.format
  implicit lazy val RepositoryFormat: OFormat[Repository] = Repository.format
  implicit lazy val FileObjectRepositoryFormat: OFormat[FileObjectRepository] = FileObjectRepository.format

  implicit val ec: ExecutionContext = defaultExecutionContext

  def listObject( id: String ) = tokenFilterAction( jwtVerifier.get ).async( parse.empty ) { implicit request =>
    val json = JsString( id )
    json.validate[UUID] match {
      case JsError( e ) => Future.successful( BadRequest( JsError.toJson( e ) ) )
      case JsSuccess( uuid, _ ) =>
        val future = db.run( dal.fileObjectRepositories.listByRepository( uuid ) )
        future.map( seq => Json.toJson( seq.map( _._2 ) ) ).map( json => Ok( json ) )
    }
  }

  def listAllObject( hash: Option[String] ) = tokenFilterAction( jwtVerifier.get ).async( parse.empty ) { implicit request =>
    db.run( hash match {
      case Some( hashValue ) =>
        dal.fileObjectRepositories.listByFileObjectHash( hashValue )
      case None =>
        dal.fileObjectRepositories.all()
    } ).map( seq => Json.toJson( seq.map {
      case ( repository, fileObjectrepository, fielObject ) =>
        Map(
          "repository" -> Json.toJson( repository ),
          "file_object_repository" -> Json.toJson( fileObjectrepository ),
          "file_object" -> Json.toJson( fielObject )
        )
    } ) ).map( json => Ok( json ) )
  }

  def createObject( id: String, oid: String ) = EssentialAction { reqh =>
    TokenFilter( jwtVerifier.get, "" ).filter( reqh ) match {
      case Right( profile ) =>
        val now = System.currentTimeMillis
        val valid = for (
          repo_id <- JsString( id ).validate[UUID].asOpt;
          obj_id <- JsString( oid ).validate[UUID].asOpt
        ) yield {
          db.run( dal.repositories.findByUUID( repo_id ) ).flatMap( f => {
            val upload = for (
              repo <- f;
              back <- backends.getBackend( repo.backend.getOrElse( default_backend ) )
            ) yield {
              val filename = reqh.headers.get( "Content-Filename" ).getOrElse( oid )
              val fo = FileObject( obj_id, "", filename, reqh.headers.get( "Content-Hash" ).getOrElse( "" ), Some( Instant.now() ), UUID.fromString( profile.getId ) )
              val fr = FileObjectRepository( obj_id, repo.uuid, Some( filename + now.toString ), Some( Instant.now() ) )
              val action = for {
                ifo <- dal.fileObjects.insert( fo )
                ifr <- dal.fileObjectRepositories.insert( fr )
              } yield ( ifo, ifr )

              def processChecksum( o: FileObject ) =
                ( _: Any, checksum: Future[String] ) =>
                  checksum.map( s => {
                    val nfo = FileObject( o.uuid, o.description, o.name, s, o.created, o.owner )
                    db.run( dal.fileObjects.update( nfo ) )
                  } )

              db.run( action.transactionally ).map { inserts =>
                back.asInstanceOf[ObjectBackend].write( reqh, repo.iid.getOrElse( "" ), filename + now.toString, processChecksum( fo ) )
              }.recoverWith { case e: SQLException => Future.successful( Accumulator.done( Conflict ) ) }
            }
            upload.getOrElse( Future.successful( Accumulator.done( NotFound ) ) )
          } )
        }
        Await.result( valid.getOrElse( Future.successful( Accumulator.done( BadRequest ) ) ), 10.seconds )

      case Left( res ) => Accumulator.done( res )
    }
  }

  def downloadObject( id: String, oid: String ) = Action.async( parse.empty ) { implicit request =>
    val valid = for (
      repo_id <- JsString( id ).validate[UUID].asOpt;
      obj_id <- JsString( oid ).validate[UUID].asOpt
    ) yield {
      db.run( dal.fileObjectRepositories.findByPk( repo_id, obj_id ) ).map( _.headOption.map {
        case ( repository, fileObject, _ ) => {
          backends.getBackend( repository.backend.getOrElse( default_backend ) ) match {
            case Some( back ) =>
              (
                for ( repo_name <- repository.iid; obj_name <- fileObject.iid ) yield {
                  back.asInstanceOf[ObjectBackend].read( request, repo_name, obj_name ) match {
                    case Some( dataContent ) => Ok.chunked( dataContent )
                    case None                => NotFound
                  }
                }
              ).getOrElse( NotFound )
            case None => BadRequest( s"The backend ${repository.backend} is not enabled." )
          }
        }
      }.getOrElse( NotFound ) )
    }
    valid.getOrElse( Future.successful( BadRequest ) )
  }

  def detailObject( id: String ) = tokenFilterAction( jwtVerifier.get ).async( parse.empty ) { implicit request =>
    val json = JsString( id )
    json.validate[UUID] match {
      case JsError( e ) => Future.successful( BadRequest( JsError.toJson( e ) ) )
      case JsSuccess( uuid, _ ) =>
        val future = db.run( dal.fileObjects.findByUUID( uuid ) )
        future map {
          case Some( obj ) => Ok( Json.toJson( obj ) )
          case None        => NotFound
        }
    }
  }

  def updateObject( id: String ) = tokenFilterAction( jwtVerifier.get ).async( bodyParseJson[FileObject] ) { implicit request =>
    val json = JsString( id )
    json.validate[UUID] match {
      case JsError( e ) => Future.successful( BadRequest( JsError.toJson( e ) ) )
      case JsSuccess( uuid, _ ) =>
        val future = db.run( dal.fileObjects.findByUUID( uuid ) )
        future flatMap {
          case Some( _ ) =>
            db.run( dal.fileObjects.update( request.body ) ).map( i => if ( i == 1 ) Ok else InternalServerError )
          case None => Future.successful( NotFound )
        }
    }
  }

}
