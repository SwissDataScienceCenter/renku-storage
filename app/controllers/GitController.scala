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
import controllers.storageBackends.{ Backends, GitBackend }
import models._
import models.persistence.DatabaseLayer
import play.api.Logger

import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.streams.Accumulator
import play.api.libs.ws._
import play.api.mvc._
import utils.ControllerWithBodyParseTolerantJson

import scala.concurrent.{ Await, Future }
/**
 * Created by jeberle on 25.04.17.
 */
@Singleton
class GitController @Inject() (
    config:                     play.api.Configuration,
    jwtVerifier:                JWTVerifierProvider,
    backends:                   Backends,
    implicit val wsclient:      WSClient,
    protected val orchestrator: DatabaseLayer
) extends Controller with ControllerWithBodyParseTolerantJson {

  lazy val logger: Logger = Logger( "application.GitController" )

  val host: String = config.getString( "renga_host" ).get
  val default_backend: String = config.getString( "lfs_default_backend" ).get

  implicit lazy val LFSBatchResponseFormat: OFormat[LFSBatchResponse] = LFSBatchResponse.format
  implicit lazy val LFSBatchResponseUpFormat: OFormat[LFSBatchResponseUp] = LFSBatchResponseUp.format

  def getRefs( id: String ) = ProfileFilterAction( jwtVerifier.get ).async { implicit request =>

    val json = JsString( id )
    json.validate[UUID] match {
      case JsError( e ) => Future( BadRequest( JsError.toJson( e ) ) )
      case JsSuccess( uuid, _ ) =>
        orchestrator.repositories.findByUUID( uuid ).flatMap {
          case Some( repo ) =>
            val backend = repo.backend
            backends.getBackend( backend ) match {
              case Some( back ) =>
                back.asInstanceOf[GitBackend].getRefs( request, repo.path, request.userId )
              case None => Future.successful( BadRequest( s"The backend $backend is not enabled." ) )
            }
          case None => Future.successful( NotFound )
        }
    }
  }

  def uploadPack( id: String ) = EssentialAction { reqh =>
    TokenFilter( jwtVerifier.get, "" ).filter( reqh ) match {
      case Right( profile ) =>
        val json = JsString( id )
        val futur = json.validate[UUID] match {
          case JsError( e ) => Future( Accumulator.done( BadRequest( JsError.toJson( e ) ) ) )
          case JsSuccess( uuid, _ ) =>
            orchestrator.repositories.findByUUID( uuid ).map {
              case Some( repo ) =>
                val backend = repo.backend
                backends.getBackend( backend ) match {
                  case Some( back ) =>
                    back.asInstanceOf[GitBackend].upload( reqh, repo.path, "" ) //profile.getId )
                  case None => Accumulator.done( BadRequest( s"The backend $backend is not enabled." ) )
                }
              case None => Accumulator.done( NotFound )
            }
        }
        Await.result( futur, 10.seconds )
      case Left( res ) => Accumulator.done( res )
    }
  }

  def receivePack( id: String ) = EssentialAction { reqh =>
    TokenFilter( jwtVerifier.get, "" ).filter( reqh ) match {
      case Right( profile ) =>
        val json = JsString( id )
        val futur = json.validate[UUID] match {
          case JsError( e ) => Future( Accumulator.done( BadRequest( JsError.toJson( e ) ) ) )
          case JsSuccess( uuid, _ ) =>
            orchestrator.repositories.findByUUID( uuid ).map {
              case Some( repo ) =>
                val backend = repo.backend
                backends.getBackend( backend ) match {
                  case Some( back ) =>
                    back.asInstanceOf[GitBackend].receive( reqh, repo.path, profile.getId )
                  case None => Accumulator.done( BadRequest( s"The backend $backend is not enabled." ) )
                }
              case None => Accumulator.done( NotFound )
            }
        }
        Await.result( futur, 10.seconds )
      case Left( res ) => Accumulator.done( res )
    }
  }

  def lfsBatch( id: String ): Action[LFSBatchRequest] = ProfileFilterAction( jwtVerifier.get ).async( bodyParseJson[LFSBatchRequest]( LFSBatchRequest.format ) ) { implicit request =>
    val token: String = request.headers.get( "Authorization" ).getOrElse( "" )
    val json = JsString( id )
    json.validate[UUID] match {
      case JsError( e ) => Future( BadRequest( JsError.toJson( e ) ) )
      case JsSuccess( uuid, _ ) =>
        if ( request.body.operation == "download" ) {
          val objects = request.body.objects.map( lfsObject => {
            orchestrator.fileobjects.findByHash( lfsObject.oid ).flatMap( _.map( fo =>
              orchestrator.fileobjectrepositories.listByFileObject( fo.uuid ).map( _.headOption.map( rep =>
                LFSObjectResponse( lfsObject.oid, lfsObject.size, true, Some( LFSDownload( host + "/api/storage/repo/" + rep._2.uuid + "/object/" + fo.uuid, token, lfsObject.oid, 600 ) ) ) ) ) ).getOrElse( Future.successful( None ) ) )
          } )
          Future.sequence( objects ).map( l => Ok( Json.toJson( LFSBatchResponse( request.body.transfers, l.filter( _.nonEmpty ).map( _.get ) ) ) ) )
        }
        else {
          orchestrator.repositories.findByUUID( uuid ).flatMap {
            case Some( repo ) => {
              val new_uuid = UUID.randomUUID()
              if ( repo.lfs_store.isEmpty ) {
                backends.getBackend( default_backend ) match {
                  case Some( back ) => {
                    back.createRepo( Repository( new_uuid, None, "automatically created bucket for LFS of " + uuid.toString, "", default_backend, None, None, None ) ).map(
                      i =>
                        i.map( iid => {
                          val rep = Repository( new_uuid, Some( iid ), "automatically created bucket for LFS of " + uuid.toString, "", default_backend, Some( Instant.now() ), Some( UUID.fromString( request.userId ) ), None )
                          orchestrator.repositories.insert( rep )
                          orchestrator.repositories.update( Repository( repo.uuid, repo.iid, repo.description, repo.path, repo.backend, repo.created, repo.owner, Some( new_uuid ) ) )
                        } )
                    )
                  }
                  case None => {}
                }
              }
              val objects = request.body.objects.map( lfsObject =>
                orchestrator.fileobjects.findByHash( lfsObject.oid ) map {
                  case Some( obj ) => Some( LFSObjectResponseUp( lfsObject.oid, lfsObject.size, true, None ) )
                  case None        => Some( LFSObjectResponseUp( lfsObject.oid, lfsObject.size, true, Some( LFSUpload( host + "/api/storage/repo/" + repo.lfs_store.getOrElse( new_uuid ) + "/object/" + UUID.randomUUID(), token, 600 ) ) ) )
                } )
              Future.sequence( objects ).map( l => Ok( Json.toJson( LFSBatchResponseUp( request.body.transfers, l.filter( _.nonEmpty ).map( _.get ) ) ) ) )
            }
            case None => Future.successful( NotFound )
          }
        }
    }
  }

}

