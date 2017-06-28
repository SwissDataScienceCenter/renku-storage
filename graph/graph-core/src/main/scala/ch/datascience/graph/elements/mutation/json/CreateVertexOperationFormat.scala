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

package ch.datascience.graph.elements.mutation.json

import ch.datascience.graph.elements.mutation.create.CreateVertexOperation
import ch.datascience.graph.elements.new_.json.NewVertexFormat
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by johann on 08/06/17.
  */
object CreateVertexOperationFormat extends Format[CreateVertexOperation] {

  def writes(op: CreateVertexOperation): JsValue = writer.writes(op)

  def reads(json: JsValue): JsResult[CreateVertexOperation] = reader.reads(json)

  private[this] lazy val writer: Writes[CreateVertexOperation] = (
    (JsPath \ "type").write[String] and
      (JsPath \ "element").write[CreateVertexOperation#ElementType]
  ) { op => ("create_vertex", op.vertex) }

  private[this] lazy val reader: Reads[CreateVertexOperation] = (
    (JsPath \ "type").read[String].filter(typeError)(_ == "create_vertex") and
      (JsPath \ "element").read[CreateVertexOperation#ElementType]
  ) { (_, vertex) => CreateVertexOperation(vertex) }

  private[this] lazy val typeError = ValidationError("expected type: 'create_vertex'")

  private[this] implicit lazy val newVertexFormat: Format[CreateVertexOperation#ElementType] = NewVertexFormat

}
