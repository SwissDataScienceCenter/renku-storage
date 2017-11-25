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

import java.util.concurrent.TimeUnit
import javax.inject._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Source, StreamConverters }
import akka.util.ByteString
import io.minio.MinioClient
import play.api.libs.concurrent.ActorSystemProvider
import play.api.libs.streams.Accumulator
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.Try
import scala.util.matching.Regex

@Singleton
class S3Backend @Inject() ( config: play.api.Configuration, actorSystemProvider: ActorSystemProvider ) extends Backend {

  private[this] val subConfig = config.getConfig( "storage.backend.s3" ).get
  lazy val minioClient = new MinioClient(
    subConfig.getString( "url" ).get,
    subConfig.getString( "access_key" ).get,
    subConfig.getString( "secret_key" ).get
  )

  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r

  def read( request: RequestHeader, bucket: String, name: String ): Option[Source[ByteString, _]] = {
    val CHUNK_SIZE = 1048576
    if ( minioClient.bucketExists( bucket ) && objectExists( bucket, name ) ) {
      val data = request.headers.get( "Range" ).map {
        case RangePattern( null, to )   => minioClient.getObject( bucket, name, 0, to.toLong )
        case RangePattern( from, null ) => minioClient.getObject( bucket, name, from.toLong )
        case RangePattern( from, to )   => minioClient.getObject( bucket, name, from.toLong, to.toLong )
      }.getOrElse( minioClient.getObject( bucket, name ) )
      Some( StreamConverters.fromInputStream( () => data, CHUNK_SIZE ) )
    }
    else {
      None
    }
  }

  def write( req: RequestHeader, bucket: String, name: String ): Accumulator[ByteString, Result] = {
    val size = req.headers.get( "Content-Length" )
    implicit val actorSystem: ActorSystem = actorSystemProvider.get
    implicit val mat: ActorMaterializer = ActorMaterializer()
    if ( minioClient.bucketExists( bucket ) )
      Accumulator.source[ByteString].mapFuture { source =>
        Future {
          val inputStream = source.runWith(
            StreamConverters.asInputStream( FiniteDuration( 3, TimeUnit.SECONDS ) )
          )
          minioClient.putObject( bucket, name, inputStream, size.get.toLong, "application/octet-stream" )
          inputStream.close()
          Created
        }
      }
    else
      Accumulator.done( NotFound )
  }

  def createBucket( request: RequestHeader, bucket: String ): String = {
    val uuid = java.util.UUID.randomUUID.toString
    minioClient.makeBucket( uuid )
    uuid
  }

  def duplicateFile( request: RequestHeader, fromBucket: String, fromName: String, toBucket: String, toName: String ): Boolean =
    Try {
      minioClient.copyObject( fromBucket, fromName, toBucket, toName )
    }.isSuccess

  def objectExists( bucket: String, name: String ): Boolean = {
    try {
      minioClient.statObject( bucket, name )
      return true
    }
    catch {
      case _: Throwable => return false
    }
  }
}
