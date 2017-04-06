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

import java.util.concurrent.TimeUnit
import javax.inject._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import io.minio.MinioClient
import play.api.mvc._
import play.api.libs.streams._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration


@Singleton
class ProxyController @Inject() extends Controller {

  // Create a minioClient with the Minio Server name, Port, Access key and Secret key.
  val minioClient = new MinioClient("https://play.minio.io:9000", "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")

  def read_object = Action { implicit request =>
    val bucket = request.getQueryString("bucket")
    val name = request.getQueryString("name")
    val CHUNK_SIZE = 100
    val data = minioClient.getObject(bucket.get, name.get)
    val dataContent: Source[ByteString, _] = StreamConverters.fromInputStream(() => data, CHUNK_SIZE)

    Ok.chunked(dataContent)
  }


  def forward: BodyParser[Result] = BodyParser { req =>
    Accumulator.source[ByteString].mapFuture { source =>
      implicit val system = ActorSystem("Sys")
      implicit val materializer = ActorMaterializer()
      val bucket = req.getQueryString("bucket")
      val name = req.getQueryString("name")
      val size = req.getQueryString("size")
      val isExist = minioClient.bucketExists(bucket.get)
      if (!isExist) minioClient.makeBucket(bucket.get)
      val inputStream = source.runWith(
        StreamConverters.asInputStream(FiniteDuration(3, TimeUnit.SECONDS))
      )
      minioClient.putObject(bucket.get, name.get, inputStream, size.get.toLong, "application/octet-stream")
      Future(Right(Ok("ok")))
    }
  }

  def write_object = Action(forward) { request =>
    request.body
  }
}