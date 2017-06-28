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

import ch.datascience.graph.types.Multiplicity
import org.janusgraph.core.EdgeLabel
import org.janusgraph.core.schema.JanusGraphManagement

/**
  * Created by johann on 07/06/17.
  */
trait EdgeLabelComponent {

  object edgeLabels {

    def getEdgeLabel(name: String): GraphManagementAction[EdgeLabel] = GraphManagementAction { mgmt: JanusGraphManagement =>
      val el = mgmt.getEdgeLabel(name)
      el match {
        case null => throw new IllegalArgumentException(s"Edge label '$name' does not exists")
        case _ => el
      }
    }

    def addEdgeLabel(name: String, multiplicity: Multiplicity): GraphManagementAction[EdgeLabel] = GraphManagementAction { mgmt: JanusGraphManagement =>
      mgmt.containsEdgeLabel(name) match {
        case true =>
          throw new IllegalArgumentException(s"Edge label '$name' already exists")
        case false =>
          val maker = mgmt.makeEdgeLabel(name)
          val janusMultiplicity = org.janusgraph.core.Multiplicity.valueOf(multiplicity.janusName.toUpperCase)
          maker.multiplicity(janusMultiplicity)
          val el = maker.make()
          el
      }

    }

  }

}
