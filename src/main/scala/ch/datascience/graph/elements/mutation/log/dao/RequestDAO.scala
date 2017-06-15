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

package ch.datascience.graph.elements.mutation.log.dao

import java.util.UUID

import ch.datascience.graph.elements.mutation.log.db.DatabaseStack
import ch.datascience.graph.elements.mutation.log.model.Event
import play.api.libs.json.JsValue
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 07/06/17.
  */
class RequestDAO(
  protected val ec: ExecutionContext,
  protected val dbConfig: DatabaseConfig[JdbcProfile],
  protected val dal: DatabaseStack
) extends DatabaseComponent {

  import profile.api._

  def all(): Future[Seq[Event]] = db.run( dal.requests.result )

  def findByIdWithResponse(id: UUID): Future[Option[(Event, Option[Event])]] = {
    db.run( dal.requests.findByIdWithResponse(id).result.headOption )
  }

  def add(event: JsValue): Future[Event] = {
    db.run( dal.requests.add(event)(ec) )
  }

}
