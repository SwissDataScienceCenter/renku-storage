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

import ch.datascience.graph.elements._
import ch.datascience.graph.types.{NamedType, PropertyKey, RecordType}

sealed trait Validated

trait ValidatedProperty[+Key, +Value, +Prop <: Property[Key, Value, Prop]] extends Validated {

  /**
    * The validated property
    * @return property
    */
  def property: Property[Key, Value, Prop]

  /**
    * The definition of the validated property
    *
    * @return property key
    */
  def propertyKey: PropertyKey[Key]

}

trait ValidatedMultiProperty[+Key, +Value, +Prop <: Property[Key, Value, Prop]] extends Validated {

  /**
    * The validated property
    * @return property
    */
  def properties: MultiPropertyValue[Key, Value, Property[Key, Value, Prop]]

  /**
    * The definition of the validated property
    *
    * @return property key
    */
  def propertyKey: PropertyKey[Key]

}

trait ValidatedRecord[Key, +Value, +Prop <: Property[Key, Value, Prop]] extends Validated {

  /**
    * The validated record
    * @return
    */
  def record: Record[Key, Value, Prop]

  /**
    * The record type of the validated record
    * @return
    */
  def recordType: RecordType[Key]

  /**
    * The definitions of the validated properties
    *
    * @return property key map
    */
  def propertyKeys: Map[Key, PropertyKey[Key]]

}

trait ValidatedMultiRecord[Key, +Value, +Prop <: Property[Key, Value, Prop]] extends Validated {

  /**
    * The validated record
    * @return
    */
  def record: MultiRecord[Key, Value, Prop]

  /**
    * The record type of the validated record
    * @return
    */
  def recordType: RecordType[Key]

  /**
    * The definitions of the validated properties
    *
    * @return property key map
    */
  def propertyKeys: Map[Key, PropertyKey[Key]]

}

trait ValidatedTypedRecord[TypeId, Key, +Value, +Prop <: Property[Key, Value, Prop]] extends Validated {

  /**
    * The validated record
    * @return
    */
  def record: TypedRecord[TypeId, Key, Value, Prop]

  /**
    * The definitions of the validated named types
    *
    * @return named type map
    */
  def namedTypes: Map[TypeId, NamedType[TypeId, Key]]

  /**
    * The record type of the validated record
    * @return
    */
  def recordType: RecordType[Key]

  /**
    * The definitions of the validated properties
    *
    * @return property key map
    */
  def propertyKeys: Map[Key, PropertyKey[Key]]

}

trait ValidatedTypedMultiRecord[TypeId, Key, +Value, +Prop <: Property[Key, Value, Prop]] extends Validated {

  /**
    * The validated record
    * @return
    */
  def record: TypedMultiRecord[TypeId, Key, Value, Prop]

  /**
    * The definitions of the validated named types
    *
    * @return named type map
    */
  def namedTypes: Map[TypeId, NamedType[TypeId, Key]]

  /**
    * The record type of the validated record
    * @return
    */
  def recordType: RecordType[Key]

  /**
    * The definitions of the validated properties
    *
    * @return property key map
    */
  def propertyKeys: Map[Key, PropertyKey[Key]]

}

trait ValidatedVertex[
TypeId,
Key,
+Value,
MetaKey,
+MetaValue,
+MetaProp <: Property[MetaKey, MetaValue, MetaProp],
+Prop <: RichProperty[Key, Value, MetaKey, MetaValue, MetaProp, Prop]
] extends Validated {

  /**
    * The validated vertex
    * @return
    */
  def vertex: Vertex[TypeId, Key, Value, MetaKey, MetaValue, MetaProp, Prop]

  /**
    * The definitions of the validated named types
    *
    * @return named type map
    */
  def namedTypes: Map[TypeId, NamedType[TypeId, Key]]

  /**
    * The record type of the validated record
    * @return
    */
  def recordType: RecordType[Key]

  /**
    * The definitions of the validated properties
    *
    * @return property key map
    */
  def propertyKeys: Map[Key, PropertyKey[Key]]

  /**
    * The definitions of the validated meta-properties
    */
  def metaPropertyKeys: Map[MetaKey, PropertyKey[MetaKey]]

}
