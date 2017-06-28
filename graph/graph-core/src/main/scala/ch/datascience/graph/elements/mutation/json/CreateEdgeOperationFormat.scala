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

import ch.datascience.graph.elements.mutation.create.CreateEdgeOperation
import ch.datascience.graph.elements.new_.json.NewEdgeFormat
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by johann on 08/06/17.
  */
object CreateEdgeOperationFormat extends Format[CreateEdgeOperation] {

  def writes(op: CreateEdgeOperation): JsValue = writer.writes(op)

  def reads(json: JsValue): JsResult[CreateEdgeOperation] = reader.reads(json)

  private[this] lazy val writer: Writes[CreateEdgeOperation] = (
    (JsPath \ "type").write[String] and
      (JsPath \ "element").write[CreateEdgeOperation#ElementType]
  ) { op => ("create_edge", op.edge) }

  private[this] lazy val reader: Reads[CreateEdgeOperation] = (
    (JsPath \ "type").read[String].filter(typeError)(_ == "create_edge") and
      (JsPath \ "element").read[CreateEdgeOperation#ElementType]
  ) { (_, edge) => CreateEdgeOperation(edge) }

  private[this] lazy val typeError = ValidationError("expected type: 'create_edge'")

  private[this] implicit lazy val newEdgeFormat: Format[CreateEdgeOperation#ElementType] = NewEdgeFormat

}
