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

package ch.datascience.graph.elements.mappers

import ch.datascience.graph.elements.{ListValue, SetValue, SingleValue}
import ch.datascience.graph.elements.persistence.{PersistedMultiRecordRichProperty, PersistedRecordProperty, PersistedVertex, VertexPath}
import ch.datascience.graph.elements.persistence.impl.ImplPersistedVertex
import ch.datascience.graph.scope.PropertyScope
import ch.datascience.graph.types.Cardinality
import ch.datascience.graph.values.BoxedValue
import org.apache.tinkerpop.gremlin.structure.{Vertex => GraphVertex}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 19/05/17.
  */
class PersistedVertexReader[Id, TypeId, Key : StringReader, PropId](scope: PropertyScope[Key])(implicit ir: Reader[java.lang.Object, Id], pir: Reader[java.lang.Object, PropId], kvr: KeyValueReader[Key, BoxedValue])
  extends Reader[GraphVertex, PersistedVertex[Id, TypeId, Key, BoxedValue, BoxedValue, PersistedRecordProperty[Key, BoxedValue], PropId, PersistedMultiRecordRichProperty[PropId, Key, BoxedValue, BoxedValue]]] {

  def read(vertex: GraphVertex)(implicit ec: ExecutionContext): Future[PersistedVertex[Id, TypeId, Key, BoxedValue, BoxedValue, PersistedRecordProperty[Key, BoxedValue], PropId, PersistedMultiRecordRichProperty[PropId, Key, BoxedValue, BoxedValue]]] = {
    //TODO: Read types

    type Prop = PersistedMultiRecordRichProperty[PropId, Key, BoxedValue, BoxedValue]
    for {
      id <- ir.read( vertex.id() )
      path = VertexPath(id)
      propertyReader = new PersistedMultiRecordRichPropertyReader[PropId, Key](path)
      props <- Future.traverse(vertex.properties[java.lang.Object]().asScala.toIterable)(propertyReader.read)
      propsByKey = props.groupBy(_.key)
      definitions <- scope.getPropertiesFor(propsByKey.keySet)
    } yield {
      val properties = for {
        (key, props) <- propsByKey
        cardinality = definitions(key).cardinality
      } yield key -> {
        cardinality match {
          case Cardinality.Single => SingleValue[Key, BoxedValue, Prop](props.head)
          case Cardinality.Set => SetValue[Key, BoxedValue, Prop](props.toList)
          case Cardinality.List => ListValue[Key, BoxedValue, Prop](props.toList)
        }
      }
      ImplPersistedVertex(id, Set.empty[TypeId], properties)
    }
  }

}
