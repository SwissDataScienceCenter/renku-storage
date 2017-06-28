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

package ch.datascience.graph.types.persistence.model.json

import java.util.UUID

import ch.datascience.graph.naming.NamespaceAndName
import ch.datascience.graph.naming.json.{NameFormat, NamespaceAndNameFormat, NamespaceFormat}
import ch.datascience.graph.types.persistence.model.{GraphDomain, RichNamedType, RichPropertyKey}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Reads, Writes}

/**
  * Created by johann on 13/06/17.
  */
object NamedTypeMappers {

  lazy val NamedTypeFormat: Format[RichNamedType] = (
    (JsPath \ "id").format[UUID](UUIDFormat) and
      (JsPath \ "graph_domain").format[GraphDomain](GraphDomainFormat) and
      (JsPath \ "name").format[String](NameFormat) and
      (JsPath \ "super_types").format[Iterable[RichNamedType]](superTypesFormat) and
      (JsPath \ "properties").format[Iterable[RichPropertyKey]](propertiesFormat)
  )(RichNamedType.apply, { nt => (nt.id, nt.graphDomain, nt.name, nt.superTypes.values, nt.properties.values) })

  lazy val NamedTypeRequestFormat: Format[(String, String, Seq[NamespaceAndName], Seq[NamespaceAndName])] = (
    (JsPath \ "namespace").format[String](NamespaceFormat) and
      (JsPath \ "name").format[String](NameFormat) and
      (JsPath \ "super_types").format[Seq[NamespaceAndName]](seqNamespaceAndNameFormatFormat) and
      (JsPath \ "properties").format[Seq[NamespaceAndName]](seqNamespaceAndNameFormatFormat)
  )(Tuple4.apply, unlift(Tuple4.unapply))

  private[this] lazy val NestedNamedTypeFormat: Format[RichNamedType] = (
    (JsPath \ "id").format[UUID](UUIDFormat) and
      (JsPath \ "graph_domain").format[GraphDomain](GraphDomainFormat) and
      (JsPath \ "name").format[String](NameFormat)
    )({ (id, gd, name) => RichNamedType(id, gd, name, Seq.empty, Seq.empty) }, { nt => (nt.id, nt.graphDomain, nt.name) })

  private[this] lazy val superTypesFormat: Format[Iterable[RichNamedType]] = {
    Format(Reads.seq(NestedNamedTypeFormat).map(x => x: Iterable[RichNamedType]), Writes.traversableWrites(NestedNamedTypeFormat))
  }

  private[this] lazy val propertiesFormat: Format[Iterable[RichPropertyKey]] = {
    Format(Reads.seq(PropertyKeyFormat).map(x => x: Iterable[RichPropertyKey]), Writes.traversableWrites(PropertyKeyFormat))
  }

  private[this] lazy val seqNamespaceAndNameFormatFormat: Format[Seq[NamespaceAndName]] = {
    Format(Reads.seq(NamespaceAndNameFormat), Writes.seq(NamespaceAndNameFormat))
  }

}
