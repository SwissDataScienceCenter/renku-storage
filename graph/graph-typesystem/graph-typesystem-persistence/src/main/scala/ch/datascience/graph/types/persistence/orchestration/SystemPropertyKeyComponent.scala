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

package ch.datascience.graph.types.persistence.orchestration

import java.util.UUID

import ch.datascience.graph.types.persistence.model.SystemPropertyKey
import ch.datascience.graph.types.{Cardinality, DataType}

import scala.concurrent.Future

/**
  * Created by johann on 04/04/17.
  */
trait SystemPropertyKeyComponent {
  this: ExecutionComponent with DatabaseComponent with GraphComponent =>

  import profile.api._

  object systemPropertyKeys {

    def all(): Future[Seq[SystemPropertyKey]] = {
      db.run( dal.systemPropertyKeys.result )
    }

    def findById(id: UUID): Future[Option[SystemPropertyKey]] = {
      db.run( dal.systemPropertyKeys.findById(id).result.headOption )
    }

    def findByName(name: String): Future[Option[SystemPropertyKey]] = {
      db.run( dal.systemPropertyKeys.findByName(name).result.headOption )
    }

    def createSystemPropertyKey(
      name: String,
      dataType: DataType = DataType.String,
      cardinality: Cardinality = Cardinality.Single
    ): Future[SystemPropertyKey] = {
      val propertyKey = SystemPropertyKey(UUID.randomUUID(), name, dataType, cardinality)
      val insertPropertyKey = dal.systemPropertyKeys add propertyKey
      val propagateChange = insertPropertyKey flatMap { _ =>
        val future = gdb.run(gal.propertyKeys.addPropertyKey(name, dataType, cardinality)).map(_ => propertyKey)
        DBIO.from(future)
      }
      db.run(propagateChange.transactionally)
    }

  }

}
