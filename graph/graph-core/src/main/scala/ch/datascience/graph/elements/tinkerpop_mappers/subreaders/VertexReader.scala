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

package ch.datascience.graph.elements.tinkerpop_mappers.subreaders

import ch.datascience.graph.elements.persisted.{PersistedVertex, PersistedVertexProperty, VertexPath}
import ch.datascience.graph.elements.tinkerpop_mappers.extracted.ExtractedVertex
import ch.datascience.graph.elements.tinkerpop_mappers.{Reader, ValueReader, VertexIdReader, TypeIdReader}
import ch.datascience.graph.elements.{ListValue, SetValue, SingleValue, tinkerpop_mappers}
import ch.datascience.graph.scope.PropertyScope
import ch.datascience.graph.types.Cardinality

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 30/05/17.
  */
case class VertexReader(scope: PropertyScope) extends Reader[ExtractedVertex, PersistedVertex] with RecordReaderHelper {

  def read(vertex: ExtractedVertex)(implicit ec: ExecutionContext): Future[PersistedVertex] = {
    for {
      id <- VertexIdReader.read(vertex.id)
      types = typePropertiesFilter(vertex.properties)
      extractedTypes <- Future.traverse(types){ prop => TypeIdReader.read(prop.value.asInstanceOf[String]) }
      path = VertexPath(id)
      properties = userPropertiesFilter(vertex.properties)
      extractedProperties <- Future.traverse(properties){ prop => VertexPropertyReader(valueReader).read((path, prop)) }
      propsByKey = extractedProperties.groupBy(_.key)
      definitions <- scope.getPropertiesFor(propsByKey.keySet)
    } yield {
      val propertiesMapped = for {
        (key, props) <- propsByKey
        cardinality = definitions(key).cardinality
      } yield key -> {
        cardinality match {
          case Cardinality.Single => SingleValue[PersistedVertexProperty](props.head)
          case Cardinality.Set => SetValue[PersistedVertexProperty](props.toList)
          case Cardinality.List => ListValue[PersistedVertexProperty](props.toList)
        }
      }
      PersistedVertex(id, extractedTypes.toSet, propertiesMapped)
    }
  }

  private[this] lazy val valueReader: ValueReader = ValueReader(scope)

}
