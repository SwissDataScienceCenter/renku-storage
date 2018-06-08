/*
 * Copyright 2018 - Swiss Data Science Center (SDSC)
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

package controllers.storageBackends

import java.io._
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import akka.util.ByteString
import javax.inject.{ Inject, Singleton }
import models.Repository
import org.eclipse.jgit.errors.{ CorruptObjectException, PackProtocolException, UnpackException }
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser
import org.eclipse.jgit.transport._
import play.api.libs.concurrent.ActorSystemProvider
import play.api.libs.streams.Accumulator
import play.api.mvc.Results._
import play.api.mvc.{ RequestHeader, Result }
import play.api.{ Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{ FiniteDuration, _ }
import scala.util.Try
/**
 * Created by julien on 01/02/18.
 */
@Singleton
class LocalGitBackend @Inject() ( configuration: Configuration, actorSystemProvider: ActorSystemProvider ) extends GitBackend {

  lazy val logger: Logger = Logger( "application.storageBackends.LocalGitBackend" )

  private[this] lazy val rootDir: String = configuration.get[String]( "storage.backend.localgit.root" )

  /*
   * derived from org.eclipse.jgit.http.server.ServletUtils.consumeRequestBody to remove dependencies on servlets
   */
  def flushBody( in: InputStream ) {
    if ( in == null )
      return
    try {
      while ( 0 < in.skip( 1024 ) || 0 <= in.read() ) {
        // Discard until EOF.
      }
    }
    catch {
      case e: IOException => {}
      // Discard IOException during read or skip.
    }
    finally {
      try {
        in.close()
      }
      catch {
        case e: IOException => {
          // Discard IOException during close of input stream.
        }
      }
    }
  }

  override def getRefs( request: RequestHeader, url: String, user: String ): Future[Result] = Future {

    val svc = request.queryString.getOrElse( "service", Seq( "" ) ).head

    val output = Try {
      StreamConverters.asOutputStream( 10.seconds ).mapMaterializedValue { os =>
        Future {
          try {
            val plo = new PacketLineOut( os )

            plo.writeString( "# service=" + svc + "\n" )
            plo.end()
            if ( svc.equals( "git-upload-pack" ) ) {
              val up = new UploadPack( FileRepositoryBuilder.create( new File( rootDir, url ) ) )
              try {
                up.setBiDirectionalPipe( false )
                up.sendAdvertisedRefs( new PacketLineOutRefAdvertiser( plo ) )
              }
              finally {
                up.getRevWalk.close()
              }
            }
            else if ( svc.equals( "git-receive-pack" ) ) {
              val rep = new ReceivePack( FileRepositoryBuilder.create( new File( rootDir, url ) ) )
              try {
                rep.sendAdvertisedRefs( new PacketLineOutRefAdvertiser( plo ) )
              }
              finally {
                rep.getRevWalk.close()
              }
            }
            os.close()
          }
          finally {
            os.close()
          }
        }
      }
    }
    output.map( o => Ok.chunked( o ).as( "application/x-" + svc + "-advertisement" ) )
      .getOrElse( InternalServerError )

  }

  override def upload( req: RequestHeader, url: String, user: String ): Accumulator[ByteString, Result] = {
    implicit val actorSystem: ActorSystem = actorSystemProvider.get
    implicit val mat: ActorMaterializer = ActorMaterializer()

    Accumulator.source[ByteString].mapFuture { source =>
      Future {

        val inputStream = source.runWith(
          StreamConverters.asInputStream( FiniteDuration( 3, TimeUnit.SECONDS ) )
        )
        val output = Try {
          StreamConverters.asOutputStream( 10.seconds ).mapMaterializedValue { os =>
            Future {
              try {
                val up = new UploadPack( FileRepositoryBuilder.create( new File( rootDir, url ) ) )
                up.setBiDirectionalPipe( false )
                up.upload( inputStream, os, null )
                os.close()
              }
              catch {
                case e: ServiceMayNotContinueException =>
                  if ( e.isOutput ) {
                    flushBody( inputStream )
                    os.close()
                  }
                  throw e
                case e: UploadPackInternalServerErrorException =>
                  // Special case exception, error message was sent to client.
                  flushBody( inputStream )
                  os.close()
              }
              finally {
                os.close()
              }
            }
          }
        }
        output.map( o => Ok.chunked( o ).as( "application/x-git-upload-pack-result" ) )
          .getOrElse( InternalServerError )
      }
    }
  }

  override def receive( req: RequestHeader, url: String, user: String ): Accumulator[ByteString, Result] = {
    implicit val actorSystem: ActorSystem = actorSystemProvider.get
    implicit val mat: ActorMaterializer = ActorMaterializer()

    Accumulator.source[ByteString].mapFuture { source =>
      Future {

        val inputStream = source.runWith(
          StreamConverters.asInputStream( FiniteDuration( 3, TimeUnit.SECONDS ) )
        )
        val output = Try {
          StreamConverters.asOutputStream( 10.seconds ).mapMaterializedValue { os =>
            Future {
              try {
                val rep = new ReceivePack( FileRepositoryBuilder.create( new File( rootDir, url ) ) )
                rep.setBiDirectionalPipe( false )
                rep.receive( inputStream, os, null )
                os.close()
              }
              catch {
                case e@( _: CorruptObjectException | _: UnpackException | _: PackProtocolException ) =>
                  flushBody( inputStream )
                  os.close()
                  throw e
              }
              finally {
                os.close()
              }
            }
          }
        }
        output.map( o => Ok.chunked( o ).as( "application/x-git-receive-pack-result" ) )
          .getOrElse( InternalServerError )
      }
    }
  }

  override def createRepo( request: Repository ): Future[Option[String]] = Future {
    if ( request.path.contains( ".." ) ) {
      None
    }
    else {
      val f = new File( rootDir, request.path )
      f.mkdirs()
      FileRepositoryBuilder.create( f ).create( true )
      Some( request.path )
    }
  }
}
