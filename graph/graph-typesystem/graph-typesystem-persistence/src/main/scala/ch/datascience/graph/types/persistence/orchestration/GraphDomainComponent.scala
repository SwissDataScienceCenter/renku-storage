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

import ch.datascience.graph.types.persistence.model.GraphDomain

import scala.concurrent.Future

/**
  * Created by johann on 04/04/17.
  */
trait GraphDomainComponent { this: DatabaseComponent with ExecutionComponent =>

  import profile.api._
  import dal._

  object graphDomains {

    def all(): Future[Seq[GraphDomain]] = db.run( dal.graphDomains.result )

    def findById(id: UUID): Future[Option[GraphDomain]] = {
      db.run( dal.graphDomains.findById(id).result.headOption )
    }

    def findByNamespace(namespace: String): Future[Option[GraphDomain]] = {
      db.run( dal.graphDomains.findByNamespace(namespace).result.headOption )
    }

    def createGraphDomain(namespace: String): Future[GraphDomain] = {
      val graphDomainId = UUID.randomUUID()
      val graphDomain = GraphDomain(graphDomainId, namespace)
      val insertDomain = dal.graphDomains add graphDomain
      db.run(insertDomain) map { _ => graphDomain }
    }

  }

}
