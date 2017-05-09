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

package ch.datascience.graph.types.persistence.graphdb

import ch.datascience.graph.types.{Cardinality, DataType}
import org.janusgraph.core.PropertyKey
import org.janusgraph.core.schema.JanusGraphManagement

/**
  * Created by johann on 21/03/17.
  */
trait PropertyKeyComponent {

  object propertyKeys {

    def getPropertyKey(name: String): GraphManagementAction[PropertyKey] = GraphManagementAction { mgmt: JanusGraphManagement =>
      val pk = mgmt.getPropertyKey(name)
      pk match {
        case null => throw new IllegalArgumentException(s"Property key '$name' does not exists")
        case _ => pk
      }
    }

    def addPropertyKey(name: String, dataType: DataType, cardinality: Cardinality): GraphManagementAction[PropertyKey] = GraphManagementAction { mgmt: JanusGraphManagement =>
      mgmt.containsPropertyKey(name) match {
        case true =>
          throw new IllegalArgumentException(s"Property key '$name' already exists")
        case false =>
          val maker = mgmt.makePropertyKey(name)
          maker.dataType(dataType.javaClass())
          val janusCardinality = org.janusgraph.core.Cardinality.valueOf(cardinality.name.toUpperCase)
          maker.cardinality(janusCardinality)
          val pk = maker.make()
          pk
      }
    }
  }

}
