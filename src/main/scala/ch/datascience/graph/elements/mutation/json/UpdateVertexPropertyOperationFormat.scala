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

import ch.datascience.graph.elements.mutation.update.UpdateVertexPropertyOperation
import ch.datascience.graph.elements.persisted.json.PersistedVertexPropertyFormat
import ch.datascience.graph.values.json.BoxedValueFormat
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by johann on 20/06/17.
  */
object UpdateVertexPropertyOperationFormat extends Format[UpdateVertexPropertyOperation] {

  private[this] lazy val writer: Writes[UpdateVertexPropertyOperation] = (
    (JsPath \ "type").write[String] and
      (JsPath \ "element").write[UpdateVertexPropertyOperation#ElementType] and
      (JsPath \ "new_value").write[UpdateVertexPropertyOperation#ElementType#Value](BoxedValueFormat)
    ) { op => ("update_vertex_property", op.vertexProperty, op.newValue) }

  private[this] lazy val reader: Reads[UpdateVertexPropertyOperation] = (
    (JsPath \ "type").read[String].filter(typeError)(_ == "update_vertex_property") and
      (JsPath \ "element").read[UpdateVertexPropertyOperation#ElementType] and
      (JsPath \ "new_value").read[UpdateVertexPropertyOperation#ElementType#Value](BoxedValueFormat)
    ) { (_, vertexProperty, newValue) => UpdateVertexPropertyOperation(vertexProperty, newValue) }

  private[this] lazy val typeError = ValidationError("expected type: 'update_vertex_property'")

  def writes(op: UpdateVertexPropertyOperation): JsValue = writer.writes(op)

  def reads(json: JsValue): JsResult[UpdateVertexPropertyOperation] = reader.reads(json)

  private[this] implicit lazy val newVertexFormat: Format[UpdateVertexPropertyOperation#ElementType] = PersistedVertexPropertyFormat

}
