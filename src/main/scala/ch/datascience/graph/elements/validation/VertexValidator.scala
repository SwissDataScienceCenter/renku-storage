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

package ch.datascience.graph.elements.validation

import ch.datascience.graph.elements.{BoxedOrValidValue, Property, RichProperty, Vertex}
import ch.datascience.graph.types.{NamedType, PropertyKey, RecordType}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 17/05/17.
  */
trait VertexValidator[
TypeId,
Key,
Value,
MetaKey,
MetaValue,
MetaProp <: Property[MetaKey, MetaValue, MetaProp],
Prop <: RichProperty[Key, Value, MetaKey, MetaValue, MetaProp, Prop]
] { this: TypedMultiRecordValidator[TypeId, Key, Value, Prop] with RecordValidator[MetaKey, MetaValue, MetaProp] =>

  def validateVertex(
    vertex: Vertex[TypeId, Key, Value, MetaKey, MetaValue, MetaProp, Prop]
  )(
    implicit e: BoxedOrValidValue[Value],
    metaE: BoxedOrValidValue[MetaValue],
    ec: ExecutionContext
  ): Future[ValidationResult[ValidatedVertex[TypeId, Key, Value, MetaKey, MetaValue, MetaProp, Prop]]] = {
    val allProperties = for {
      propertyValue <- vertex.properties.values
      property <- propertyValue
    } yield property.lifted.lifted

    for {
      typedMultiRecordValidation <- this.validateTypedMultiRecord(vertex)
      propertiesAsRecordValidation <- Future.traverse(allProperties) { property => this.validateRecord(property) }
      allErrors = (propertiesAsRecordValidation ++ Seq(typedMultiRecordValidation)).flatMap(_.left.toOption)
    } yield {
      if (allErrors.isEmpty) {
        val validTypedMultiRecord = typedMultiRecordValidation.right.get
        val validMetaProperties = for {
          validated <- propertiesAsRecordValidation
          v <- validated.right.toSeq
          metaPropertyKey <- v.propertyKeys
        } yield metaPropertyKey
        Right(Result(vertex, validTypedMultiRecord.namedTypes, validTypedMultiRecord.recordType, validTypedMultiRecord.propertyKeys, validMetaProperties.toMap))
      }
      else
        Left(MultipleErrors.make(allErrors.toSeq))
    }
  }

  private[this] case class Result(
    vertex: Vertex[TypeId, Key, Value, MetaKey, MetaValue, MetaProp, Prop],
    namedTypes: Map[TypeId, NamedType[TypeId, Key]],
    recordType: RecordType[Key],
    propertyKeys: Map[Key, PropertyKey[Key]],
    metaPropertyKeys: Map[MetaKey, PropertyKey[MetaKey]]
  ) extends ValidatedVertex[TypeId, Key, Value, MetaKey, MetaValue, MetaProp, Prop]

}
