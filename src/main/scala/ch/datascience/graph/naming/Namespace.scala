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

package ch.datascience.graph.naming

import scala.util.matching.Regex
import scala.util.{Success, Try}

/**
  * Created by jmt on 15/05/17.
  */
object Namespace {

  lazy val namespacePattern: Regex = raw"([-A-Za-z0-9_/.]*)".r

  def apply(namespace: String): String = {
    require(namespaceIsValid(namespace), s"Invalid namespace: '$namespace' (Pattern: $namespacePattern)")
    namespace
  }

  def unapply(namespace: String): Option[String] = Try( apply(namespace) ) match {
    case Success(ns) => Some(ns)
    case _           => None
  }

  def namespaceIsValid(namespace: String): Boolean = namespace match {
    case namespacePattern(_) => true
    case _                   => false
  }

}
