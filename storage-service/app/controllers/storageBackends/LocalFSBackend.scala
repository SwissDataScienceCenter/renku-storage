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

import java.io.{File, FileInputStream, FileNotFoundException, FileOutputStream}
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import play.api.libs.concurrent.ActorSystemProvider
import play.api.libs.concurrent.Execution.defaultContext
import play.api.mvc.RequestHeader

import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.util.matching.Regex

/**
  * Created by johann on 07/07/17.
  */
@Singleton
class LocalFSBackend @Inject()(actorSystemProvider: ActorSystemProvider) extends Backend {

  override def read(request: RequestHeader, bucket: String, name: String): Option[Source[ByteString, _]] = {
    Try {
      val fullPath = s"$bucket/$name"
      val (from, to) = getRange(request)

      val is = new FileInputStream(fullPath)
      for (n <- from) {
        is.skip(n)
      }

      val dataContent: Source[Byte, _] = StreamConverters.fromInputStream(() => is).mapConcat(identity)
      val dataContent2: Source[Byte, _] = (from, to) match {
        case (Some(n), Some(m)) => dataContent.take(m - n)
        case (None, Some(m)) => dataContent.take(m)
        case _ => dataContent
      }

      Some(dataContent2)
    }.recover {
      case _: FileNotFoundException | _: SecurityException => None
    }.get
  }


  override def write(req: RequestHeader, bucket: String, name: String, source: Source[ByteString, _]): Boolean = {
    implicit val actorSystem: ActorSystem  = actorSystemProvider.get
    implicit val mat: ActorMaterializer = ActorMaterializer()

    val fullPath = s"$bucket/$name"
    val os = new FileOutputStream(fullPath)
    val sink = StreamConverters.fromOutputStream(() => os)
    source.runWith(sink)
    true
  }

  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r

  def getRange(request: RequestHeader): (Option[Int], Option[Int]) = {
    val opt = request.headers.get("Range").map {
      case RangePattern(null, to) => (None, Some(to.toInt))
      case RangePattern(from, null) => (Some(from.toInt), None)
      case RangePattern(from, to) => (Some(from.toInt), Some(to.toInt))
      case _ => (None, None)
    }
    opt.getOrElse((None, None))
  }

  private[this] implicit lazy val ex: ExecutionContext = defaultContext

  override def createBucket(request: RequestHeader, bucket: String): Boolean = {
    new File(bucket).mkdir()
  }
}
