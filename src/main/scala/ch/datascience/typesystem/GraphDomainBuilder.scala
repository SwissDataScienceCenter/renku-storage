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

package ch.datascience.typesystem

/**
  * Created by johann on 07/03/17.
  */
class GraphDomainBuilder(private val _authority: Option[Authority],
                         private val _namespace: Option[String],
                         private val _version: Int,
                         private val _edgeLabels: Map[String, GraphDomain.EdgeLabel],
                         private val _propertyKeys: Map[String, GraphDomain.PropertyKey],
                         private val _constraints: Set[Constraint]) {

  def this() {
    this(None, None, 0, Map(), Map(), Set())
  }

  def authority(authority: Authority): GraphDomainBuilder = {
    new GraphDomainBuilder(Some(authority), _namespace, _version, _edgeLabels, _propertyKeys, _constraints)
  }

  def namespace(namespace: String): GraphDomainBuilder = {
    new GraphDomainBuilder(_authority, Some(namespace), _version, _edgeLabels, _propertyKeys, _constraints)
  }

  def version(version: Int): GraphDomainBuilder = {
    new GraphDomainBuilder(_authority, _namespace, version, _edgeLabels, _propertyKeys, _constraints)
  }

  def addEdgeLabel(label: GraphDomain.EdgeLabel): GraphDomainBuilder = {
    val key = s"${label.name}:${label.version}"
    new GraphDomainBuilder(_authority, _namespace, _version, _edgeLabels + (key -> label), _propertyKeys, _constraints)
  }

  def addPropertyKey(propertyKey: GraphDomain.PropertyKey): GraphDomainBuilder = {
    val key = s"${propertyKey.name}:${propertyKey.version}"
    new GraphDomainBuilder(_authority, _namespace, _version, _edgeLabels, _propertyKeys + (key -> propertyKey), _constraints)
  }

  def addConstraint(constraint: Constraint): GraphDomainBuilder = {
    new GraphDomainBuilder(_authority, _namespace, _version, _edgeLabels, _propertyKeys, _constraints + constraint)
  }

  def make(): GraphDomain = {
    if (_authority.isEmpty) throw new IllegalArgumentException("authority must be provided")
    if (_namespace.isEmpty) throw new IllegalArgumentException("namespace must be provided")

    GraphDomainImpl(_authority.get, _namespace.get, _version, _edgeLabels, _propertyKeys, _constraints)
  }

  case class GraphDomainImpl(authority: Authority,
                             namespace: String,
                             version: Int,
                             edgeLabels: Map[String, GraphDomain.EdgeLabel],
                             propertyKeys: Map[String, GraphDomain.PropertyKey],
                             constraints: Set[Constraint]) extends GraphDomain


  }
