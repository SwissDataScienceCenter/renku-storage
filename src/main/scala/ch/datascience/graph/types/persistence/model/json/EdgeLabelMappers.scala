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

import ch.datascience.graph.naming.json.{NameFormat, NamespaceFormat}
import ch.datascience.graph.types.Multiplicity
import ch.datascience.graph.types.json.MultiplicityFormat
import ch.datascience.graph.types.persistence.model.{GraphDomain, RichEdgeLabel}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

/**
  * Created by johann on 13/06/17.
  */
object EdgeLabelMappers {

  lazy val EdgeLabelFormat: Format[RichEdgeLabel] = (
    (JsPath \ "id").format[UUID](UUIDFormat) and
      (JsPath \ "graph_domain").format[GraphDomain](GraphDomainFormat) and
      (JsPath \ "name").format[String](NameFormat) and
      (JsPath \ "multiplicity").format[Multiplicity](MultiplicityFormat)
  )(RichEdgeLabel.apply, unlift(RichEdgeLabel.unapply))

  lazy val EdgeLabelRequestFormat: Format[(String, String, Multiplicity)] = (
    (JsPath \ "namespace").format[String](NamespaceFormat) and
      (JsPath \ "name").format[String](NameFormat) and
      (JsPath \ "multiplicity").format[Multiplicity](MultiplicityFormat)
    )(Tuple3.apply, unlift(Tuple3.unapply))

}
