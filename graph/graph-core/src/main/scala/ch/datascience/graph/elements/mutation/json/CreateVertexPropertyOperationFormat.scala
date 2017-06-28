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

import ch.datascience.graph.elements.mutation.create.CreateVertexPropertyOperation
import ch.datascience.graph.elements.new_.json.NewRichPropertyFormat
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by johann on 08/06/17.
  */
object CreateVertexPropertyOperationFormat extends Format[CreateVertexPropertyOperation] {

  def writes(op: CreateVertexPropertyOperation): JsValue = writer.writes(op)

  def reads(json: JsValue): JsResult[CreateVertexPropertyOperation] = reader.reads(json)

  private[this] lazy val writer: Writes[CreateVertexPropertyOperation] = (
    (JsPath \ "type").write[String] and
      (JsPath \ "element").write[CreateVertexPropertyOperation#ElementType]
  ) { op => ("create_vertex_property", op.vertexProperty) }

  private[this] lazy val reader: Reads[CreateVertexPropertyOperation] = (
    (JsPath \ "type").read[String].filter(typeError)(_ == "create_vertex_property") and
      (JsPath \ "element").read[CreateVertexPropertyOperation#ElementType]
  ) { (_, vertexProperty) => CreateVertexPropertyOperation(vertexProperty) }

  private[this] lazy val typeError = ValidationError("expected type: 'create_vertex_property'")

  private[this] implicit lazy val newVertexFormat: Format[CreateVertexPropertyOperation#ElementType] = NewRichPropertyFormat

}
