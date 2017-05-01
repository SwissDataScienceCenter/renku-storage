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

package ch.datascience.graph

import scala.util.matching.Regex

/**
  * Created by johann on 30/04/17.
  */
case class NamespaceAndName(namespace: String, name: String) {
  require(NamespaceAndName.namespaceIsValid(namespace), s"Invalid namespace: '$namespace' (Pattern: ${NamespaceAndName.namespacePattern})")
  require(NamespaceAndName.nameIsValid(name), s"Invalid name: '$name' (Pattern: ${NamespaceAndName.namePattern})")

  def asString: String = s"$namespace:$name"

}

object NamespaceAndName {

  lazy val namespacePattern: Regex = raw"([-A-Za-z0-9_/]*)".r
  lazy val namePattern: Regex = raw"([-A-Za-z0-9_/]+)".r
  lazy val separatePattern: Regex = s"([^:]*):(.*)".r

  def apply(namespaceAndName: String): NamespaceAndName = namespaceAndName match {
    case separatePattern(namespace, name) => NamespaceAndName(namespace, name)
    case _ => throw new IllegalArgumentException(s"Cannot find separator ':'")
  }

  def namespaceIsValid(namespace: String): Boolean = namespace match {
    case namespacePattern(_) => true
    case _ => false
  }

  def nameIsValid(name: String): Boolean = name match {
    case namePattern(_) => true
    case _ => false
  }

}
