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

import ch.datascience.typesystem.{Cardinality, DataType}
import ch.datascience.typesystem.model.row.{GraphDomain, PropertyKey}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Created by johann on 04/04/17.
  */
trait PropertyKeyOrchestrator { this: DatabaseComponent with GraphComponent =>

  import dal.profile.api._

  def createPropertyKey(graphDomain: GraphDomain,
                        name: String,
                        dataType: DataType = DataType.String,
                        cardinality: Cardinality = Cardinality.Single)(implicit ec: ExecutionContext): Future[PropertyKey] = {
    val fullname = s"${graphDomain.namespace}:$name"
    val propertyKeyId = UUID.randomUUID()
    val propertyKey = PropertyKey(propertyKeyId, graphDomain.id, name, dataType, cardinality)
    val insertPropertyKey = dal.propertyKeys add propertyKey
    val propagateChange = insertPropertyKey map { _ =>
      val future = gal.run(gal.addPropertyKey(fullname, dataType, cardinality)).map(_ => propertyKey)
      Await.result(future, Duration.Inf)
    }
    db.run(propagateChange.transactionally)
  }

  def createPropertyKey(namespace: String,
                        name: String,
                        dataType: DataType,
                        cardinality: Cardinality)(implicit ec: ExecutionContext): Future[PropertyKey] = {
    val selectGraphDomain = db.run(dal.graphDomains.findByNamespace(namespace).result.headOption.map(_.get))
    selectGraphDomain.flatMap(graphDomain => createPropertyKey(graphDomain, name, dataType, cardinality))
  }

}
