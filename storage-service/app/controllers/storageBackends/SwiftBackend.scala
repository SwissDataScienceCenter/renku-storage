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
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import org.javaswift.joss.client.factory.{AccountConfig, AccountFactory}
import org.javaswift.joss.headers.`object`.range.{FirstPartRange, LastPartRange, MidPartRange}
import org.javaswift.joss.instructions.DownloadInstructions
import org.javaswift.joss.model.Account
import play.api.libs.concurrent.ActorSystemProvider

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.streams.Accumulator
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.matching.Regex


@Singleton
class SwiftBackend @Inject()(config: play.api.Configuration, actorSystemProvider: ActorSystemProvider) extends Backend {

  val swiftConfig = new AccountConfig()
  private[this] val subConfig = config.getConfig("storage.backend.swift").get
  swiftConfig.setUsername(subConfig.getString("username").get)
  swiftConfig.setPassword(subConfig.getString("password").get)
  swiftConfig.setAuthUrl(subConfig.getString("auth_url").get)
  swiftConfig.setTenantId(subConfig.getString("project").get)
  lazy val swiftAccount: Account = new AccountFactory(swiftConfig).createAccount()

  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r


  def read(request: RequestHeader, bucket: String, name: String): Option[Source[ByteString, _]] = {
    val CHUNK_SIZE = 100
    val container = swiftAccount.getContainer(bucket)
    if (container.exists() && container.getObject(name).exists()) {
      val instructions = new DownloadInstructions()
      request.headers.get("Range").map {
        case RangePattern(null, to) => instructions.setRange(new FirstPartRange(to.toInt))
        case RangePattern(from, null) => instructions.setRange(new LastPartRange(from.toInt))
        case RangePattern(from, to) => instructions.setRange(new MidPartRange(from.toInt, to.toInt))
        case _ =>
      }
      val data = container.getObject(name).downloadObjectAsInputStream(instructions)
      Some(StreamConverters.fromInputStream(() => data, CHUNK_SIZE))
    } else {
      None
    }
  }

  def write(req: RequestHeader, bucket: String, name: String): Accumulator[ByteString, Result] = {

    implicit val actorSystem: ActorSystem  = actorSystemProvider.get
    implicit val mat: ActorMaterializer = ActorMaterializer()
    val container = swiftAccount.getContainer(bucket)
    if (container.exists())
      Accumulator.source[ByteString].mapFuture { source =>
        Future {
          val obj = container.getObject(name)
          val inputStream = source.runWith(
            StreamConverters.asInputStream(FiniteDuration(3, TimeUnit.SECONDS))
          )
          obj.uploadObject(inputStream)
          inputStream.close()
          Created
        }
    }
    else
      Accumulator.done(NotFound)
  }

  def createBucket(request: RequestHeader, bucket: String): String = {
    val uuid = java.util.UUID.randomUUID.toString
    val container = swiftAccount.getContainer(uuid)
    container.create()
    uuid
  }
}