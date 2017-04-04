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

package ch.datascience.typesystem.orchestration

import java.util.UUID

import ch.datascience.typesystem.model.row.GraphDomain

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 04/04/17.
  */
trait GraphDomainOrchestrator { this: DatabaseComponent =>

  def createGraphDomain(namespace: String)(implicit ec: ExecutionContext): Future[GraphDomain] = {
    val graphDomainId = UUID.randomUUID()
    val graphDomain = GraphDomain(graphDomainId, namespace)
    val insertDomain = dal.graphDomains add graphDomain
    db.run(insertDomain) map { _ => graphDomain }
  }

}
