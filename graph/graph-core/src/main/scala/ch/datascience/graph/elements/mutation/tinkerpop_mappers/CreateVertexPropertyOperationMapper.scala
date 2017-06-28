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
import ch.datascience.graph.elements.mutation.create.{CreateVertexOperation, CreateVertexPropertyOperation}
import ch.datascience.graph.elements.tinkerpop_mappers.{CardinalityWriter, KeyWriter, TypeIdWriter, ValueWriter}
import ch.datascience.graph.types.Cardinality
import org.apache.tinkerpop.gremlin.process.traversal.Traverser
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.{GraphTraversal, GraphTraversalSource}
import org.apache.tinkerpop.gremlin.structure.{Vertex, VertexProperty}

/**
  * Created by johann on 30/05/17.
  */
case object CreateVertexPropertyOperationMapper extends Mapper {

  final type OperationType = CreateVertexPropertyOperation

  final type Source = Vertex
  final type Element = Vertex

  def apply(op: CreateVertexPropertyOperation): (GraphTraversalSource) => GraphTraversal[Vertex, Vertex] = {
    val vertexProperty = op.vertexProperty
    val parent = vertexProperty.parent

    { s: GraphTraversalSource =>
      // Get parent
      val t1 = PathHelper.follow(s, parent).asInstanceOf[GraphTraversal[Vertex, Vertex]]

      val card = s.getGraph.features().vertex().getCardinality(KeyWriter.write(vertexProperty.key))
      card match {
        case VertexProperty.Cardinality.single =>
          val t2 = t1.map(singleFilter(KeyWriter.write(vertexProperty.key)))
          addProperty(t2, card, vertexProperty)
        case VertexProperty.Cardinality.set =>
          val t2 = t1.map(setFilter(KeyWriter.write(vertexProperty.key), ValueWriter.write(vertexProperty.value)))
          addProperty(t2, card, vertexProperty)
        case VertexProperty.Cardinality.list => addProperty(t1, card, vertexProperty)
      }
    }
  }

  private[this] type VertexPropertyType = OperationType#ElementType

  private[this] def singleFilter(key: String): java.util.function.Function[Traverser[Vertex], Vertex] = new java.util.function.Function[Traverser[Vertex], Vertex] {
    def apply(t: Traverser[Vertex]): Vertex = {
      import scala.collection.JavaConverters._
      val vertex = t.get()
      if (vertex.properties(key).asScala.nonEmpty)
        throw new IllegalArgumentException(s"Property already exists: $key")
      vertex
    }
  }

  private[this] def setFilter(key: String, value: java.lang.Object): java.util.function.Function[Traverser[Vertex], Vertex] = new java.util.function.Function[Traverser[Vertex], Vertex] {
    def apply(t: Traverser[Vertex]): Vertex = {
      import scala.collection.JavaConverters._
      val vertex = t.get()
      if (vertex.properties[java.lang.Object](key).asScala.exists(_.value() == value))
        throw new IllegalArgumentException(s"Property already exists: $key, $value")
      vertex
    }
  }

  private[this] def addProperty(t: GraphTraversal[Vertex, Vertex], card: VertexProperty.Cardinality, prop: VertexPropertyType): GraphTraversal[Vertex, Vertex] = {
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
