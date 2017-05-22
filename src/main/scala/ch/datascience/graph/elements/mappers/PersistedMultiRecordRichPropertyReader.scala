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
import ch.datascience.graph.elements.persistence.impl.ImplPersistedMultiRecordRichProperty
import ch.datascience.graph.elements.persistence.{Path, PropertyPathFromMultiRecord}
import ch.datascience.graph.scope.PropertyScope
import org.apache.tinkerpop.gremlin.structure.{VertexProperty => GraphMultiRecordRichProperty}

import scala.collection.JavaConverters.asScalaIteratorConverter
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 22/05/17.
  */
//Id, Key, Value: BoxedOrValidValue, MetaKey, MetaValue: BoxedOrValidValue
class PersistedMultiRecordRichPropertyReader[Id, Key : StringReader, MetaKey : StringReader](val parent: Path)(implicit ir: Reader[java.lang.Object, Id], kvr: KeyValueReader[Key, BoxedValue], kvr2: KeyValueReader[MetaKey, BoxedValue])
  extends Reader[GraphMultiRecordRichProperty[java.lang.Object],  ImplPersistedMultiRecordRichProperty[Id, Key, BoxedValue, MetaKey, BoxedValue]] {

  def read(property: GraphMultiRecordRichProperty[java.lang.Object])(implicit ec: ExecutionContext): Future[ImplPersistedMultiRecordRichProperty[Id, Key, BoxedValue, MetaKey, BoxedValue]] = {
    for {
      id <- ir.read(property.id())
      key <- implicitly[StringReader[Key]].read(property.key())
      value <- kvr.read(key -> property.value())
      path = PropertyPathFromMultiRecord(parent, id)
      metaPropertyReader = new PersistedRecordPropertyReader[MetaKey](path)
      metaProps <- Future.traverse(property.properties[java.lang.Object]().asScala)(metaPropertyReader.read)
    } yield {
      val metaProperties = (for {
        metaProperty <- metaProps
      } yield metaProperty.key -> metaProperty).toMap
      ImplPersistedMultiRecordRichProperty(parent, id, key, value, metaProperties)
    }
  }

}
