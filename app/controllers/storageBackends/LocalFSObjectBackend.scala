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

import java.io.{ File, FileInputStream, FileNotFoundException, FileOutputStream }
import java.nio.file.{ FileSystems, Files }

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Source, StreamConverters }
import akka.util.ByteString
import javax.inject.{ Inject, Singleton }
import models.Repository
import play.api.Configuration
import play.api.libs.concurrent.ActorSystemProvider
import play.api.libs.streams.Accumulator
import play.api.mvc.Results.Created
import play.api.mvc.{ RequestHeader, Result }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try
import scala.util.matching.Regex

/**
 * Created by johann on 07/07/17.
 */
@Singleton
class LocalFSObjectBackend @Inject() (
    configuration:       Configuration,
    actorSystemProvider: ActorSystemProvider,
    implicit val ec:     ExecutionContext
) extends ObjectBackend {

  private[this] lazy val rootDir: String = configuration.get[String]( "storage.backend.local.root" )

  def read( request: RequestHeader, bucket: String, name: String ): Option[Source[ByteString, _]] = {
    Try {
      val fullPath = s"$rootDir/$bucket/$name"
      val ( from, to ) = getRange( request )

      val is = new FileInputStream( fullPath )
      for ( n <- from ) {
        is.skip( n )
      }

      val dataContent: Source[ByteString, _] = StreamConverters.fromInputStream( () => is )
      val dataContent2: Source[ByteString, _] = ( from, to ) match {
        case ( Some( n ), Some( m ) ) =>
          takeFromByteStringSource( dataContent, m - n )
        case ( None, Some( m ) ) =>
          takeFromByteStringSource( dataContent, m )
        case _ => dataContent
      }

      Some( dataContent2 )
    }.recover {
      case _: FileNotFoundException | _: SecurityException => None
    }.get
  }

  def write( req: RequestHeader, bucket: String, name: String, callback: ( Any, Future[String] ) => Any ): Accumulator[ByteString, Result] = {
    implicit val actorSystem: ActorSystem = actorSystemProvider.get
    implicit val mat: ActorMaterializer = ActorMaterializer()
    Accumulator.source[ByteString].mapFuture { source =>
      val fullPath = s"$rootDir/$bucket/$name"
      new File( fullPath ).getParentFile.mkdirs()
      val os = new FileOutputStream( fullPath )
      val sink = StreamConverters.fromOutputStream( () => os )
      val r = source.alsoToMat( new ChecksumSink() )( callback ).runWith( sink )
      r.map( _ => Created )
    }
  }

  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r

  def getRange( request: RequestHeader ): ( Option[Int], Option[Int] ) = {
    val opt = request.headers.get( "Range" ).map {
      case RangePattern( null, to )   => ( None, Some( to.toInt ) )
      case RangePattern( from, null ) => ( Some( from.toInt ), None )
      case RangePattern( from, to )   => ( Some( from.toInt ), Some( to.toInt ) )
      case _                          => ( None, None )
    }
    opt.getOrElse( ( None, None ) )
  }

  def createBucket( request: RequestHeader, bucket: String ): String = {
    new File( rootDir, bucket ).mkdirs()
    bucket
  }

  def createRepo( request: Repository ): Future[Option[String]] = Future {
    Try {
      new File( rootDir, request.path ).mkdirs()
      request.path
    }.toOption
  }

  def duplicateFile( request: RequestHeader, fromBucket: String, fromName: String, toBucket: String, toName: String ): Boolean = Try {

    val source = FileSystems.getDefault.getPath( s"$rootDir/$fromBucket/$fromName" )
    val dest = FileSystems.getDefault.getPath( s"$rootDir/$toBucket/$toName" )

    Files.copy( source, dest )

  }.isSuccess

  private[this] def takeFromByteStringSource( source: Source[ByteString, _], n: Int, chunkSize: Int = 8192 ): Source[ByteString, _] = {
    source.mapConcat( identity ).take( n ).grouped( chunkSize ).map { bytes => ByteString( bytes: _* ) }
  }

}
