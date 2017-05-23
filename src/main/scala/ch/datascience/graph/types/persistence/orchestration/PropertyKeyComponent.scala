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

import ch.datascience.graph.naming.NamespaceAndName
import ch.datascience.graph.types.persistence.model.{GraphDomain, PropertyKey, RichPropertyKey}
import ch.datascience.graph.types.{Cardinality, DataType}

import scala.concurrent.Future

/**
  * Created by johann on 04/04/17.
  */
trait PropertyKeyComponent {
  this: ExecutionComponent with DatabaseComponent with GraphComponent =>

  import profile.api._
  import dal._

  object propertyKeys {

    def all(): Future[Seq[RichPropertyKey]] = {
      db.run( dal.propertyKeys.mapped.result )
    }

    def findById(id: UUID): Future[Option[RichPropertyKey]] = {
      db.run( dal.propertyKeys.findById(id).result.headOption )
    }

    def findByNamespaceAndName(key: NamespaceAndName): Future[Option[RichPropertyKey]] = {
      findByNamespaceAndName(key.namespace, key.name)
    }

    def findByNamespaceAndName(namespace: String, name: String): Future[Option[RichPropertyKey]] = {
      db.run( dal.propertyKeys.findByNamespaceAndName(namespace, name).result.headOption )
    }

    def createPropertyKey(
      graphDomain: GraphDomain,
      name: String,
      dataType: DataType = DataType.String,
      cardinality: Cardinality = Cardinality.Single
    ): Future[RichPropertyKey] = {
      val fullname = s"${graphDomain.namespace}:$name"
      val propertyKey = RichPropertyKey(UUID.randomUUID(), graphDomain, name, dataType, cardinality)
      val insertPropertyKey = dal.propertyKeys add propertyKey
      val propagateChange = insertPropertyKey flatMap { _ =>
        val future = gdb.run(gal.propertyKeys.addPropertyKey(fullname, dataType, cardinality)).map(_ => propertyKey)
        DBIO.from(future)
      }
      db.run(propagateChange.transactionally)
    }

    def createPropertyKey(
      namespace: String,
      name: String,
      dataType: DataType,
      cardinality: Cardinality
    ): Future[RichPropertyKey] = {
      val selectGraphDomain = db.run( dal.graphDomains.findByNamespace(namespace).result.headOption.map(_.get) )
      selectGraphDomain flatMap { graphDomain => createPropertyKey(graphDomain, name, dataType, cardinality) }
    }

  }

}
