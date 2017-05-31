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

package ch.datascience.graph.elements.tinkerpop_mappers.extractors

import ch.datascience.graph.elements.tinkerpop_mappers.extracted.ExtractedEdge
import org.apache.tinkerpop.gremlin.structure.Edge

import scala.collection.JavaConverters._

/**
  * Created by johann on 30/05/17.
  */
object EdgeExtractor extends Extractor[Edge, ExtractedEdge] {

  def apply(edge: Edge): ExtractedEdge = {
    val properties = edge.properties[java.lang.Object]().asScala.toList
    val extractedProperties = for {
      prop <- properties
    } yield LeafPropertyExtractor(prop)
    // outVertex ---label---> inVertex
    val from = edge.outVertex().id()
    val to = edge.inVertex().id()
    ExtractedEdge(edge.id(), edge.label(), from, to, extractedProperties)
  }

}
