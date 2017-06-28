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

package ch.datascience.graph.elements.tinkerpop_mappers

import ch.datascience.graph.elements.persisted.PersistedEdge
import ch.datascience.graph.elements.tinkerpop_mappers.extractors.{EdgeExtractor, VertexExtractor}
import ch.datascience.graph.elements.tinkerpop_mappers.subreaders.{EdgeReader => EdgeSubReader}
import ch.datascience.graph.scope.PropertyScope
import org.apache.tinkerpop.gremlin.structure.Edge

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 30/05/17.
  */
case class EdgeReader(scope: PropertyScope) extends Reader[Edge, PersistedEdge] {

  override def read(edge: Edge)(implicit ec: ExecutionContext): Future[PersistedEdge] = {
    val extractedEdge = EdgeExtractor(edge)
    reader.read(extractedEdge)
  }

  private[this] lazy val reader: EdgeSubReader = EdgeSubReader(scope)

}
