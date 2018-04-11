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

import javax.inject.{ Inject, Singleton }

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import play.api.{ Configuration, Logger }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.util.Try

@Singleton
class HealthCheckController @Inject() () extends Controller {

  def ping: Action[Unit] = Action.async( BodyParsers.parse.empty ) { implicit request =>
    // Perform health check
    Future {
      Ok
    }
  }

}

object HealthCheckController {

  /**
   * Invokes the health check
   */
  def main( args: Array[String] ): Unit = {
    lazy val logger: Logger = Logger( "application.HealthCheckController" )

    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val config: Configuration = Configuration( ConfigFactory.load() )
    val ws: WSClient = AhcWSClient()

    val prefix: String = config.getString( "play.http.context" ).getOrElse( "/" )
    val checkURL: String = s"http://localhost:9000$prefix/ping"

    val futureStatus = for {
      response <- ws.url( checkURL ).get()
    } yield response.status match {
      case 200 => Done
      case _   => throw new RuntimeException( response.statusText )
    }

    val try_ = Try {
      Await.result( futureStatus, 10.seconds )
    }

    if ( try_.isSuccess ) {
      System.exit( 0 )
    }
    else {
      logger.error( try_.failed.get.getMessage )
      Thread.sleep( 10 )
      System.exit( 1 )
    }
  }

}
