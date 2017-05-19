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

import ch.datascience.graph.elements.BoxedValue
import ch.datascience.graph.elements.builders.PersistedVertexBuilder
import ch.datascience.graph.elements.persistence.impl.ImplPersistedVertex
import ch.datascience.graph.scope.{NamedTypeScope, PropertyScope}
import org.apache.tinkerpop.gremlin.structure.{Vertex => GraphVertex}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 19/05/17.
  */
class PersistedVertexReader[Id, TypeId, Key : StringReader, MetaKey : StringReader, PropId](scope: PropertyScope[Key], metaScope: PropertyScope[MetaKey])(implicit ir: Reader[java.lang.Object, Id], kvr: KeyValueReader[Key, BoxedValue], kvr2: KeyValueReader[MetaKey, BoxedValue])
  extends Reader[GraphVertex, ImplPersistedVertex[Id, TypeId, Key, BoxedValue, MetaKey, BoxedValue, PropId]] {

  def read(vertex: GraphVertex)(implicit ec: ExecutionContext): Future[ImplPersistedVertex[Id, TypeId, Key, BoxedValue, MetaKey, BoxedValue, PropId]] = {
    val builder = new PersistedVertexBuilder[Id, TypeId, Key, BoxedValue, MetaKey, BoxedValue, PropId]()
    for {
      id <- ir.read( vertex.id() )
      //TODO: Read properties
      //card <- scope.getPropertiesFor(  )
    } yield {
      builder.id = id
      // ???
      builder.result()
    }
  }

}
