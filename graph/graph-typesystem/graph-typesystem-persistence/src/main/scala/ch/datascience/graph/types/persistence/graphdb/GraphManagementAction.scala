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

import org.janusgraph.core.schema.JanusGraphManagement

/**
  * Created by johann on 22/03/17.
  */
trait GraphManagementAction[+A] extends (JanusGraphManagement => A) {

  def map[B](f: (A) => B): GraphManagementAction[B]

  def flatMap[B](f: (A) => GraphManagementAction[B]): GraphManagementAction[B]

}

object GraphManagementAction {

  def apply[A](f: =>(JanusGraphManagement) => A): GraphManagementAction[A] = new GraphManagementActionImpl(f)

  private[this] class GraphManagementActionImpl[A](action: (JanusGraphManagement) => A) extends GraphManagementAction[A] {

    override def map[B](f: (A) => B): GraphManagementAction[B] = GraphManagementAction { this andThen f }

    override def flatMap[B](f: (A) => GraphManagementAction[B]): GraphManagementAction[B] = GraphManagementAction { mgmt: JanusGraphManagement =>
      val actionB: GraphManagementAction[B] = f(this(mgmt))
      actionB(mgmt)
    }

    override def apply(mgmt: JanusGraphManagement): A = action(mgmt)

  }

}
