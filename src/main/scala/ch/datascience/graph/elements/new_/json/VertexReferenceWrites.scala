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
import play.api.libs.json.{JsPath, JsValue, Writes}
import play.api.libs.functional.syntax._

/**
  * Created by johann on 08/06/17.
  */
object VertexReferenceWrites extends Writes[NewEdge#VertexReference] {

  def writes(ref: Either[NewEdge#NewVertexType#TempId, NewEdge#PersistedVertexType#Id]): JsValue = ref match {
    case Left(tempId) => leftWrites.writes(tempId)
    case Right(id) => rightWrites.writes(id)
  }

  private[this] lazy val leftWrites: Writes[NewEdge#NewVertexType#TempId] = (
    (JsPath \ "type").write[String] and
      (JsPath \ "id").write[NewEdge#NewVertexType#TempId]
    ) { tempId => ("new_vertex", tempId) }

  private[this] lazy val rightWrites: Writes[NewEdge#PersistedVertexType#Id] = (
    (JsPath \ "type").write[String] and
      (JsPath \ "id").write[NewEdge#PersistedVertexType#Id]
    ) { id => ("persisted_vertex", id) }

}
