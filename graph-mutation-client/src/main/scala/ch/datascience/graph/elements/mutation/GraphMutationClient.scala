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

package ch.datascience.graph.elements.mutation

import java.util.UUID

import ch.datascience.graph.elements.mutation.log.model.{Event, EventStatus}
import play.api.libs.ws.WSClient

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Created by johann on 28/06/17.
  */
trait GraphMutationClient {

  def baseUrl: String

  def post(mutation: Mutation): Future[Event]

  /**
    *
    * @param uuid
    * @param timeout soft timeout, won't attempt again if expired
    * @return
    */
  def wait(uuid: UUID, timeout: Option[Deadline] = Some(1.minute.fromNow)): Future[EventStatus]

  final def wait(uuid: UUID, timeout: Duration)(implicit ec: ExecutionContext): Future[EventStatus] = timeout match {
    case d: FiniteDuration => wait(uuid, Some(d.fromNow))
    case Duration.Inf => Future {
      Await.result(wait(uuid, None), timeout)
    }
    case _ => Future {
      Await.result(wait(uuid, Some(10.seconds.fromNow)), timeout)
    }
  }

  def status(uuid: UUID): Future[EventStatus]

}

object GraphMutationClient {

  def apply(baseUrl: String, context: ExecutionContext, ws: WSClient): GraphMutationClient = new ImplGraphMutationClient(baseUrl, context, ws)

  def makeStandaloneClient(baseUrl: String): GraphMutationClient with AutoCloseable = {
    import akka.actor.ActorSystem
    import akka.stream.ActorMaterializer
    import play.api.libs.ws.ahc.AhcWSClient

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val wsClient = AhcWSClient()

    val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

    class StandaloneGraphMutationClient extends ImplGraphMutationClient(baseUrl, ec, wsClient) with AutoCloseable {
      def close(): Unit = {
        wsClient.close()
        materializer.shutdown()
        system.terminate()
      }
    }

    new StandaloneGraphMutationClient
  }

}
