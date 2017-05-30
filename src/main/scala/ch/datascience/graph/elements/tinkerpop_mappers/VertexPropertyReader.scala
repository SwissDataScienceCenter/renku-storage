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

import ch.datascience.graph.elements.persisted.{PersistedVertexProperty, VertexPath}
import ch.datascience.graph.elements.tinkerpop_mappers.extractors.VertexPropertyExtractor
import ch.datascience.graph.elements.tinkerpop_mappers.subreaders.{VertexPropertyReader => VertexPropertySubReader}
import ch.datascience.graph.scope.PropertyScope
import org.apache.tinkerpop.gremlin.structure.VertexProperty

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 30/05/17.
  */
case class VertexPropertyReader(scope: PropertyScope) extends Reader[VertexProperty[java.lang.Object], PersistedVertexProperty] {

  def read(prop: VertexProperty[Object])(implicit ec: ExecutionContext): Future[PersistedVertexProperty] = {
    val extractedProperty = VertexPropertyExtractor(prop)
    val parent = prop.element()

    for {
      id <- VertexIdReader.read(parent.id())
      parentPath = VertexPath(id)
      mappedVertex <- reader.read((parentPath, extractedProperty))
    } yield mappedVertex
  }

  private[this] lazy val valueReader: ValueReader = ValueReader(scope)

  private[this] lazy val reader: VertexPropertySubReader = VertexPropertySubReader(valueReader)

}
