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

import java.io.{ PipedInputStream, PipedOutputStream }
import java.util.concurrent.TimeUnit

import javax.inject._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Keep
import akka.stream.scaladsl.{ Source, StreamConverters }
import akka.util.ByteString
import ch.datascience.service.security.RequestWithProfile
import com.microsoft.azure.storage._
import com.microsoft.azure.storage.blob.{ CloudBlobClient, CopyStatus }
import models.Repository
import play.api.Logger
import play.api.libs.concurrent.ActorSystemProvider
import play.api.libs.streams.Accumulator
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.blocking
import scala.concurrent.duration._
import scala.util.Try
import scala.util.matching.Regex

@Singleton
class AzureObjectBackend @Inject() ( config: play.api.Configuration, actorSystemProvider: ActorSystemProvider ) extends ObjectBackend {

  private[this] val subConfig = config.getConfig( "storage.backend.azure" ).get

  lazy val account: CloudStorageAccount = CloudStorageAccount.parse( subConfig.getString( "connection_string" ).get )
  lazy val serviceClient: CloudBlobClient = account.createCloudBlobClient

  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r

  def read( request: RequestHeader, bucket: String, name: String ): Option[Source[ByteString, _]] = {
    val container = serviceClient.getContainerReference( bucket )
    if ( container.exists() ) {
      val blob = container.getBlockBlobReference( name )
      if ( blob.exists() ) {
        Some( StreamConverters.fromInputStream( () => {

          val CHUNK_SIZE = 1048576

          // Pipe for getting data from the download thread
          val outputStream = new PipedOutputStream()
          val inputStream = new PipedInputStream( outputStream )

          // listening to the download completion to close the pipe
          val oc = new OperationContext()
          val sem = new StorageEventMultiCaster[RequestCompletedEvent, StorageEvent[RequestCompletedEvent]]()

          sem.addListener( new StorageEvent[RequestCompletedEvent] {
            override def eventOccurred( eventArg: RequestCompletedEvent ): Unit = {
              outputStream.close()
            }
          } )
          oc.setRequestCompletedEventHandler( sem )

          // asynchronously start the download
          new Thread( new Runnable {
            override def run() = {
              request.headers.get( "Range" ) match {
                case Some( RangePattern( null, to ) )   => blob.downloadRange( 0, to.toLong, outputStream, null, null, oc )
                case Some( RangePattern( from, null ) ) => blob.downloadRange( from.toLong, null, outputStream, null, null, oc )
                case Some( RangePattern( from, to ) )   => blob.downloadRange( from.toLong, to.toLong, outputStream, null, null, oc )
                case _                                  => blob.downloadRange( 0, null, outputStream, null, null, oc )
              }
            }
          } ).start()

          inputStream

        } ) )
      }
      else {
        None
      }
    }
    else {
      None
    }
  }

  def write( req: RequestHeader, bucket: String, name: String, hash: Option[String] ): Accumulator[ByteString, Result] = {
    implicit val actorSystem: ActorSystem = actorSystemProvider.get
    implicit val mat: ActorMaterializer = ActorMaterializer()
    val container = serviceClient.getContainerReference( bucket )
    val size = req.headers.get( "Content-Length" )
    if ( container.exists() ) {
      Accumulator.source[ByteString].mapFuture { source =>
        Future {
          val inputStream = source.alsoToMat( new ChecksumSink() )( processChecksum( hash ) ).runWith(
            StreamConverters.asInputStream( FiniteDuration( 3, TimeUnit.SECONDS ) )
          )
          val blob = container.getBlockBlobReference( name )
          // for some reason the declared size cannot be exactly the size of the input !!
          blob.upload( inputStream, size.map( _.toLong.+( 1 ) ).getOrElse( -1 ) )
          inputStream.close()
          Created
        }
      }
    }
    else
      Accumulator.done( NotFound )
  }

  def createRepo( request: Repository ): Future[Option[String]] = Future {
    Try {
      val uuid = request.uuid.toString
      serviceClient.getContainerReference( uuid ).createIfNotExists()
      uuid
    }.toOption
  }

  def duplicateFile( request: RequestHeader, fromBucket: String, fromName: String, toBucket: String, toName: String ): Boolean = {
    val fromContainer = serviceClient.getContainerReference( fromBucket )
    val toContainer = serviceClient.getContainerReference( toBucket )
    ( fromContainer.exists() && toContainer.exists() ) && {
      val fromBlob = fromContainer.getBlockBlobReference( fromName )
      val toBlob = toContainer.getBlockBlobReference( toName )
      toBlob.startCopy( fromBlob )

      def waitForIt( timeout: Deadline ): Future[Boolean] = {
        toBlob.downloadAttributes()
        if ( toBlob.getCopyState.getStatus ne CopyStatus.PENDING ) {

          if ( timeout.hasTimeLeft() )
            Future {
              blocking( Thread.sleep( 1000 ) )
            }.flatMap { _ => waitForIt( timeout ) }
          else
            Future.successful( false )
        }
        else {
          Future.successful( toBlob.getCopyState.getStatus eq CopyStatus.SUCCESS )
        }
      }
      Await.result( waitForIt( Deadline( 5.minutes ) ), Duration.Inf )
    }
  }

}
