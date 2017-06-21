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

/**
  * Created by johann on 14/04/17.
  */
sealed abstract class EntityType(val name: String)

object EntityType {

  def apply(name: String): EntityType = name.toLowerCase match {
    case GraphDomain.name => GraphDomain
    case PropertyKey.name => PropertyKey
    case SystemPropertyKey.name => SystemPropertyKey
    case NamedType.name => NamedType
    case EdgeLabel.name => EdgeLabel
  }

  case object GraphDomain extends EntityType(name = "graph_domain")

  case object PropertyKey extends EntityType(name = "property_key")

  case object SystemPropertyKey extends EntityType(name = "system_property_key")

  case object NamedType extends EntityType(name = "named_type")

  case object EdgeLabel extends EntityType(name = "edge_label")

  def valueOf(name: String): EntityType = EntityType.apply(name)

}
