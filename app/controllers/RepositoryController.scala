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
import ch.datascience.service.security.ProfileFilterAction
import ch.datascience.service.utils.ControllerWithBodyParseTolerantJson
import controllers.storageBackends.Backends
import models._
import models.persistence.DatabaseLayer
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

/**
 * Created by jeberle on 25.04.17.
 */
@Singleton
class RepositoryController @Inject() (
    config:                     play.api.Configuration,
    jwtVerifier:                JWTVerifierProvider,
    backends:                   Backends,
    protected val dal:          DatabaseLayer

) extends Controller with ControllerWithBodyParseTolerantJson {

  lazy val logger: Logger = Logger( "application.RepositoryController" )

  implicit lazy val RepositoryFormat: OFormat[Repository] = Repository.format

  def listRepo() = ProfileFilterAction( jwtVerifier.get ).async( BodyParsers.parse.empty ) { implicit request =>
    val all = dal.repositories.all()
    all.map( seq => Json.toJson( seq ) ).map( json => Ok( json ) )
  }

  def createRepo() = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[Repository] ) { implicit request =>
    backends.getBackend( request.body.backend ) match {
      case Some( back ) => {
        back.createRepo( request.body ).flatMap(
          i =>
            i.map( iid => {
              val rep = Repository( request.body.uuid, Some( iid ), request.body.description, request.body.path, request.body.backend, Some( Instant.now() ), Some( UUID.fromString( request.userId ) ), request.body.lfs_store )
              dal.repositories.insert( rep ).map(i => if ( i == 1 ) Created else InternalServerError )
            } ).getOrElse( Future.successful( BadRequest ) )
        )
      }
      case None => Future.successful( NotFound )
    }
  }

  def detailRepo( id: String ) = ProfileFilterAction( jwtVerifier.get ).async( BodyParsers.parse.empty ) { implicit request =>
    val json = JsString( id )
    json.validate[UUID] match {
      case JsError( e ) => Future.successful( BadRequest( JsError.toJson( e ) ) )
      case JsSuccess( uuid, _ ) =>
        val future = dal.repositories.findByUUID( uuid )
        future map {
          case Some( repo ) => Ok( Json.toJson( repo ) )
          case None         => NotFound
        }
    }
  }

  def updateRepo( id: String ) = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[Repository] ) { implicit request =>
    val json = JsString( id )
    json.validate[UUID] match {
      case JsError( e ) => Future.successful( BadRequest( JsError.toJson( e ) ) )
      case JsSuccess( uuid, _ ) =>
        val future = dal.repositories.findByUUID( uuid )
        future flatMap {
          case Some( _ ) => {
            dal.repositories.update( request.body ).map(i => if ( i == 1 ) Ok else InternalServerError )
          }
          case None => Future.successful( NotFound )
        }
    }
  }

  def repoBackends = ProfileFilterAction( jwtVerifier.get ).async { implicit request =>
    Future( Ok( Json.toJson( backends.map.keys ) ) )
  }

}

