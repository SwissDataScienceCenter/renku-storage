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

package ch.datascience.graph.types.persistence.model

import java.util.UUID
import ch.datascience.graph.types.{NamedType => StandardNamedType}
import ch.datascience.graph.naming.NamespaceAndName

/**
  * Created by johann on 23/05/17.
  */
class RichNamedType(
  id: UUID,
  val graphDomain: GraphDomain,
  name: String,
  val superTypes: Map[NamespaceAndName, RichNamedType],
  val properties: Map[NamespaceAndName, RichPropertyKey]
) extends NamedType(id, graphDomain.id, name)
    with RichAbstractEntity[NamedType] {

  def key: NamespaceAndName = NamespaceAndName(graphDomain.namespace, name)

  def toStandardNamedType: StandardNamedType = StandardNamedType(key, superTypes.keySet, properties.keySet)

}

object RichNamedType {

  def apply(id: UUID, graphDomain: GraphDomain, name: String, superTypes: Map[NamespaceAndName, RichNamedType], properties: Map[NamespaceAndName, RichPropertyKey]): RichNamedType = {
    new RichNamedType(id, graphDomain, name, superTypes, properties)
  }

  def apply(id: UUID, graphDomain: GraphDomain, name: String, superTypes: Iterable[RichNamedType], properties: Iterable[RichPropertyKey]): RichNamedType = {
    val superTypesMap = for {
      superType <- superTypes
    } yield superType.key -> superType
    val propertiesMap = for {
      property <- properties
    } yield property.key -> property
    new RichNamedType(id, graphDomain, name, superTypesMap.toMap, propertiesMap.toMap)
  }

  def unapply(namedType: RichNamedType): Option[(UUID, GraphDomain, String, Map[NamespaceAndName, RichNamedType], Map[NamespaceAndName, RichPropertyKey])] = {
    if (namedType eq null)
      None
    else
      Some((namedType.id, namedType.graphDomain, namedType.name, namedType.superTypes, namedType.properties))
  }

}
