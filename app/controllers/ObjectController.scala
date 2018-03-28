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

import java.time.Instant
import java.util.UUID
import javax.inject.{ Inject, Singleton }

import authorization.JWTVerifierProvider
import ch.datascience.service.security.{ ProfileFilterAction, TokenFilter }
import ch.datascience.service.utils.ControllerWithBodyParseJson
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{ BodyParsers, Controller, EssentialAction }
import controllers.storageBackends.{ Backends, ObjectBackend }
import models.{ FileObject, FileObjectRepository }
import models.persistence.DatabaseLayer
import play.api.Logger
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json._

import scala.concurrent.duration._
import play.api.libs.streams.Accumulator
import slick.jdbc.JdbcProfile

import scala.concurrent.{ Await, Future }

/**
 * Created by jeberle on 25.04.17.
 */
@Singleton
class ObjectController @Inject() (
    config:            play.api.Configuration,
    jwtVerifier:       JWTVerifierProvider,
    backends:          Backends,
    protected val dal: DatabaseLayer
) extends Controller with ControllerWithBodyParseJson with HasDatabaseConfig[JdbcProfile] {

  override protected val dbConfig = dal.dbConfig
  import profile.api._

  lazy val logger: Logger = Logger( "application.AuthorizeController" )
  val default_backend: String = config.getString( "lfs_default_backend" ).get

  implicit lazy val FileObjectFormat: OFormat[FileObject] = FileObject.format

  def listObject( id: String ) = ProfileFilterAction( jwtVerifier.get ).async( BodyParsers.parse.empty ) { implicit request =>
    val json = JsString( id )
    json.validate[UUID] match {
      case JsError( e ) => Future.successful( BadRequest( JsError.toJson( e ) ) )
      case JsSuccess( uuid, _ ) =>
        val future = db.run( dal.fileObjectRepositories.listByRepository( uuid ) )
        future.map( seq => Json.toJson( seq.map( _._2 ) ) ).map( json => Ok( json ) )
    }
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
              db.run( action.transactionally ).map( inserts =>
                if ( inserts._1 == 1 && inserts._2 == 1 )
                  back.asInstanceOf[ObjectBackend].write( reqh, repo.iid.getOrElse( "" ), filename + now.toString )
                else
                  Accumulator.done( BadRequest ) )
            }
            upload.getOrElse( Future.successful( Accumulator.done( NotFound ) ) )
          } )
        }
        Await.result( valid.getOrElse( Future.successful( Accumulator.done( BadRequest ) ) ), 10.seconds )

      case Left( res ) => Accumulator.done( res )
    }
  }

  def downloadObject( id: String, oid: String ) = ProfileFilterAction( jwtVerifier.get ).async( BodyParsers.parse.empty ) { implicit request =>
    val valid = for (
      repo_id <- JsString( id ).validate[UUID].asOpt;
      obj_id <- JsString( oid ).validate[UUID].asOpt
    ) yield {
      db.run( dal.fileObjectRepositories.findByPk( repo_id, obj_id ) ).map( _.headOption.map( f =>
        backends.getBackend( f._1.backend.getOrElse( default_backend ) ) match {
          case Some( back ) =>
            (
              for ( repo_name <- f._1.iid; obj_name <- f._2.iid ) yield {
                back.asInstanceOf[ObjectBackend].read( request, repo_name, obj_name ) match {
                  case Some( dataContent ) => Ok.chunked( dataContent )
                  case None                => NotFound
                }
              }
            ).getOrElse( NotFound )
          case None => BadRequest( s"The backend ${f._1.backend} is not enabled." )
        } ).getOrElse( NotFound ) )
    }
    valid.getOrElse( Future.successful( BadRequest ) )
  }

  def detailObject( id: String ) = ProfileFilterAction( jwtVerifier.get ).async( BodyParsers.parse.empty ) { implicit request =>
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

  def updateObject( id: String ) = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[FileObject] ) { implicit request =>
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
