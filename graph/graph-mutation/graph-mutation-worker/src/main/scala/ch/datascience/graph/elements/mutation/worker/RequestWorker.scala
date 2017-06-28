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

package ch.datascience.graph.elements.mutation.worker

import java.util.UUID

import ch.datascience.graph.elements.mutation.log.dao.RequestDAO
import ch.datascience.graph.elements.mutation.log.model.Event
import play.api.libs.json.JsValue

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 07/06/17.
  */
class RequestWorker(
  protected val queue: Queue[(UUID, JsValue)],
  protected val dao: RequestDAO,
  protected val ec: ExecutionContext
) {

  def add(event: JsValue): Future[Event] = dao.add(event).map { event =>
    queue.enqueue((event.uuid, event.event))
    event
  }

  private[this] implicit lazy val e: ExecutionContext = ec

}
