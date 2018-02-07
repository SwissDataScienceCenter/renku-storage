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

package controllers.storageBackends

import java.io._
import java.util.concurrent.TimeUnit
import javax.inject.{ Inject, Singleton }

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import akka.util.ByteString
import play.api.Configuration
import play.api.libs.concurrent.ActorSystemProvider
import play.api.libs.streams.Accumulator
import play.api.mvc.{ RequestHeader, Result }

import scala.concurrent.Future
import scala.util.Try
import play.api.mvc.Results._
import org.eclipse.jgit.util.TemporaryBuffer
import org.eclipse.jgit.transport._
import java.util.zip.GZIPOutputStream

import ch.datascience.service.security.RequestWithProfile
import models.Repository
import org.eclipse.jgit.errors.{ CorruptObjectException, PackProtocolException, UnpackException }
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
/**
 * Created by johann on 07/07/17.
 */
@Singleton
class LocalGitBackend @Inject() ( configuration: Configuration, actorSystemProvider: ActorSystemProvider ) extends GitBackend {

  private[this] lazy val rootDir: String = configuration.getString( "storage.backend.localgit.root" ).get

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

  /*
   * derived from SmartOutputStream to remove dependencies to servlet request and response objects
   */
  class ZippedBuffer( outputStream: OutputStream, compress: Boolean ) extends TemporaryBuffer( 32 * 1024 ) {

    private[this] var startedOutput = false
    var compressed: Boolean = compress
    var len: Long = 0

    override def overflow(): OutputStream = {
      startedOutput = true
      if ( compress ) new GZIPOutputStream( outputStream ) else outputStream
    }

    override def flush(): Unit = {
      doFlush()
    }

    override def close(): Unit = {
      super.close()
      if ( !startedOutput ) {

        val out = if ( 256 < this.length() && compress ) {
          val gzbuf: TemporaryBuffer = new TemporaryBuffer.Heap( 32 * 1024 )
          Try {
            val gzip: GZIPOutputStream = new GZIPOutputStream( gzbuf )
            try {
              this.writeTo( gzip, null )
            }
            finally {
              gzip.close()
            }
            if ( gzbuf.length() < this.length() ) {
              compressed = true
              gzbuf
            }
            else {
              compressed = false
              this
            }
          }.getOrElse( this )
        }
        else { this }

        len = out.length()
        try {
          out.writeTo( outputStream, null )
          outputStream.flush()
        }
        finally {
          outputStream.close()
        }
      }
    }
  }

  override def getRefs( request: RequestHeader, url: String, user: String ): Future[Result] = Future {

    val svc = request.queryString.getOrElse( "service", Seq( "" ) ).head

    val output = Try {
      StreamConverters.asOutputStream().mapMaterializedValue { os =>
        val out = new ZippedBuffer( os, true )
        try {
          val plo = new PacketLineOut( out )

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
          out.close()
        }
        catch {
          case e: ServiceMayNotContinueException =>
            if ( e.isOutput ) {
              out.close()
            }
            throw e
        }
      }
    }
    output.map( o => Ok.chunked( o ).withHeaders( "Content-Type" -> ( "application/x-" + svc + "-advertisement" ) ) )
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
          StreamConverters.asOutputStream().mapMaterializedValue { os =>
            val out = new ZippedBuffer( os, false )
            try {
              val up = new UploadPack( FileRepositoryBuilder.create( new File( rootDir, url ) ) )
              up.setBiDirectionalPipe( false )
              up.upload( inputStream, out, null )
              out.close()
            }
            catch {
              case e: ServiceMayNotContinueException =>
                if ( e.isOutput ) {
                  flushBody( inputStream )
                  out.close()
                }
                throw e
              case e: UploadPackInternalServerErrorException =>
                // Special case exception, error message was sent to client.
                flushBody( inputStream )
                out.close()
            }
          }
        }
        output.map( o => Ok.chunked( o ).withHeaders( "Content-Type" -> "application/x-git-upload-pack-result" ) )
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
          StreamConverters.asOutputStream().mapMaterializedValue { os =>
            val out = new ZippedBuffer( os, false )
            try {
              val rep = new ReceivePack( FileRepositoryBuilder.create( new File( rootDir, url ) ) )
              rep.setBiDirectionalPipe( false )
              rep.receive( inputStream, out, null )
              out.close()
            }
            catch {
              case e@( _: CorruptObjectException | _: UnpackException | _: PackProtocolException ) =>
                flushBody( inputStream )
                out.close()
                throw e
            }
          }
        }
        output.map( o => Ok.chunked( o ).withHeaders( "Content-Type" -> "application/x-git-receive-pack-result" ) )
          .getOrElse( InternalServerError )
      }
    }
  }

  override def createRepo( request: RequestWithProfile[Repository] ): Future[Option[String]] = Future {
    if ( request.body.path.contains( ".." ) ) {
      None
    }
    else {
      val f = new File( rootDir, request.body.path )
      f.mkdirs()
      FileRepositoryBuilder.create( f ).create( true )
      Some( request.body.path )
    }
  }
}
