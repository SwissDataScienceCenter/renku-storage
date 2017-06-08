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

import ch.datascience.graph.Constants
import ch.datascience.graph.elements.mutation.create.CreateVertexOperation
import ch.datascience.graph.elements.tinkerpop_mappers.{CardinalityWriter, KeyWriter, TypeIdWriter, ValueWriter}
import ch.datascience.graph.types.Cardinality
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.{GraphTraversal, GraphTraversalSource}
import org.apache.tinkerpop.gremlin.structure.{Vertex, VertexProperty}

/**
  * Created by johann on 30/05/17.
  */
case object CreateVertexOperationMapper extends Mapper {

  final type OperationType = CreateVertexOperation

  final type Source = Vertex
  final type Element = Vertex

  def apply(op: CreateVertexOperation): (GraphTraversalSource) => GraphTraversal[Vertex, Vertex] = {
    val vertex = op.vertex

    { s: GraphTraversalSource =>
      // Add vertex
      val t1 = s.addV()

      // Add types
      val t2 = vertex.types.foldLeft(t1) { (t, typeId) => t.addType(typeId) }

      // Add properties
      val t3 = vertex.properties.values.foldLeft(t2) { (t, multiPropValue) =>
        multiPropValue.foldLeft(t) { (tt, prop) =>
          tt.addProperty(multiPropValue.cardinality, prop)
        }
      }

      // We don't iterate here
      t3
    }
  }

  private[this] type VertexType = OperationType#ElementType

  private[this] implicit class RichVertexTraversal(t: GraphTraversal[Vertex, Vertex]) {

    def addType(typeId: VertexType#TypeId): GraphTraversal[Vertex, Vertex] = {
      val value = TypeIdWriter.write(typeId)
      val props = Seq.empty
      t.property(VertexProperty.Cardinality.set, Constants.TypeKey, value, props: _*)
    }

    def addProperty(cardinality: Cardinality, prop: VertexType#Prop): GraphTraversal[Vertex, Vertex] = {
      val card = CardinalityWriter.write(cardinality)
      val key = KeyWriter.write(prop.key)
      val value = ValueWriter.write(prop.value)
      val keyValues: Seq[java.lang.Object] = (for {
        metaProp <- prop.properties.values
        metaKey = KeyWriter.write(metaProp.key)
        metaValue = ValueWriter.write(metaProp.value)
        x <- Seq(metaKey, metaValue)
      } yield x).toSeq

      t.property(card, key, value, keyValues: _*)
    }

  }

}
