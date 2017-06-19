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

package ch.datascience.graph.elements.mutation.tinkerpop_mappers

import ch.datascience.graph.elements.persisted._
import ch.datascience.graph.elements.tinkerpop_mappers.KeyWriter
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.{GraphTraversal, GraphTraversalSource}
import org.apache.tinkerpop.gremlin.structure.{Edge, Vertex}

/**
  * Created by johann on 19/06/17.
  */
object PathHelper {

  def follow(s: GraphTraversalSource, path: Path): GraphTraversal[_, _] = path match {
    case VertexPath(id) => s.V(Long.box(id))
    case EdgePath(id) => s.E(id)
    case PropertyPathFromRecord(parent, key) => follow(s, parent).properties(KeyWriter.write(key))
    case VertexPropertyPath(parent, id) => follow(s, parent).properties().hasId(id)
  }

}
