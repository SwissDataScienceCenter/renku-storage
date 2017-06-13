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

import ch.datascience.graph.naming.json.NamespaceFormat
import ch.datascience.graph.types.persistence.model.GraphDomain
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by johann on 13/06/17.
  */
object GraphDomainMappers {

  lazy val GraphDomainFormat: Format[GraphDomain] = (
    (JsPath \ "id").format[UUID](UUIDFormat) and
      (JsPath \ "namespace").format[String](NamespaceFormat)
  )(GraphDomain.apply, unlift(GraphDomain.unapply))

  lazy val GraphDomainRequestFormat: Format[String] = Format(graphDomainRequestReads, (JsPath \ "namespace").write[String])

  private[this] def graphDomainRequestReads: Reads[String] = Reads { json =>
    (JsPath \ "namespace").read[String](NamespaceFormat).reads(json) match {
      case JsSuccess(ns, _) => JsSuccess(ns)
      case e: JsError => e
    }
  }

}
