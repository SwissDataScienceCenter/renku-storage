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

package ch.datascience.graph.types

/**
  * Created by johann on 09/04/17.
  */
sealed abstract class Cardinality(val name: String)

object Cardinality {

  def apply(name: String): Cardinality = name.toLowerCase match {
    case Single.name => Single
    case List.name => List
    case Set.name => Set
  }

  case object Single extends Cardinality(name = "single")

  case object List extends Cardinality(name = "list")

  case object Set extends Cardinality(name = "set")

  def valueOf(name: String): Cardinality = Cardinality.apply(name)

}
