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

trait ValidatedProperty extends Validated {

  /**
    * The validated property
    * @return property
    */
  def property: Property

  /**
    * The definition of the validated property
    *
    * @return property key
    */
  def propertyKey: PropertyKey

}

trait ValidatedMultiProperty extends Validated {

  /**
    * The validated property
    * @return property
    */
  def properties: MultiPropertyValue[Property]

  /**
    * The definition of the validated property
    *
    * @return property key
    */
  def propertyKey: PropertyKey

}

trait ValidatedRecord extends Validated {

  /**
    * The validated record
    * @return
    */
  def record: Record

  /**
    * The record type of the validated record
    * @return
    */
  def recordType: RecordType

  /**
    * The definitions of the validated properties
    *
    * @return property key map
    */
  def propertyKeys: Map[PropertyKey#Key, PropertyKey]

}

trait ValidatedMultiRecord extends Validated {

  /**
    * The validated record
    * @return
    */
  def record: MultiRecord

  /**
    * The record type of the validated record
    * @return
    */
  def recordType: RecordType

  /**
    * The definitions of the validated properties
    *
    * @return property key map
    */
  def propertyKeys: Map[PropertyKey#Key, PropertyKey]

}

trait ValidatedTypedRecord extends Validated {

  /**
    * The validated record
    * @return
    */
  def record: TypedRecord

  /**
    * The definitions of the validated named types
    *
    * @return named type map
    */
  def namedTypes: Map[NamedType#TypeId, NamedType]

  /**
    * The record type of the validated record
    * @return
    */
  def recordType: RecordType

  /**
    * The definitions of the validated properties
    *
    * @return property key map
    */
  def propertyKeys: Map[PropertyKey#Key, PropertyKey]

}

trait ValidatedTypedMultiRecord extends Validated {

  /**
    * The validated record
    * @return
    */
  def record: TypedMultiRecord

  /**
    * The definitions of the validated named types
    *
    * @return named type map
    */
  def namedTypes: Map[NamedType#TypeId, NamedType]

  /**
    * The record type of the validated record
    * @return
    */
  def recordType: RecordType

  /**
    * The definitions of the validated properties
    *
    * @return property key map
    */
  def propertyKeys: Map[PropertyKey#Key, PropertyKey]

}

trait ValidatedVertex extends Validated {

  /**
    * The validated vertex
    * @return
    */
  def vertex: Vertex

  /**
    * The definitions of the validated named types
    *
    * @return named type map
    */
  def namedTypes: Map[NamedType#TypeId, NamedType]

  /**
    * The record type of the validated record
    * @return
    */
  def recordType: RecordType

  /**
    * The definitions of the validated properties
    *
    * @return property key map
    */
  def propertyKeys: Map[PropertyKey#Key, PropertyKey]

  /**
    * The definitions of the validated meta-properties
    */
  def metaPropertyKeys: Map[PropertyKey#Key, PropertyKey]

}
