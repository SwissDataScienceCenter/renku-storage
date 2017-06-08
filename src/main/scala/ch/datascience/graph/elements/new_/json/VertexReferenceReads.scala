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

package ch.datascience.graph.elements.new_.json

import ch.datascience.graph.elements.new_.NewEdge
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by johann on 08/06/17.
  */
object VertexReferenceReads extends Reads[NewEdge#VertexReference] {

  def reads(json: JsValue): JsResult[Either[NewEdge#NewVertexType#TempId, NewEdge#PersistedVertexType#Id]] = self.reads(json)

  private[this] lazy val self: Reads[Either[NewEdge#NewVertexType#TempId, NewEdge#PersistedVertexType#Id]] = typeReads.flatMap {
    case Left(()) => (
      (JsPath \ "type").read[String] and
        (JsPath \ "id").read[NewEdge#NewVertexType#TempId]
      ) { (_, tempId) => Left(tempId) }
    case Right(()) => (
      (JsPath \ "type").read[String] and
        (JsPath \ "id").read[NewEdge#PersistedVertexType#Id]
      ) { (_, id) => Right(id) }
  }

  private[this] lazy val typeReads: Reads[Either[Unit, Unit]] = Reads { json =>
    (JsPath \ "type").read[String].reads(json).flatMap {
      case "new_vertex" => JsSuccess(Left(()))
      case "persisted_vertex" => JsSuccess(Right(()))
      case _ => JsError("type must be 'new_vertex' or 'persisted_vertex'")
    }
  }

}
