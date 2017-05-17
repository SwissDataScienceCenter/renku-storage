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

package ch.datascience.graph.types.json

import ch.datascience.graph.types.NamedType
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsResult, JsValue, Reads}

/**
  * Created by johann on 17/05/17.
  */
class NamedTypeReads[TypeKey : Reads, PropKey : Reads] extends Reads[NamedType[TypeKey, PropKey]] {

  override def reads(json: JsValue): JsResult[NamedType[TypeKey, PropKey]] = self.reads(json)

  private[this] lazy val self: Reads[NamedType[TypeKey, PropKey]] = makeSelf

  private[this] def makeSelf: Reads[NamedType[TypeKey, PropKey]] = (
    (JsPath \ "key").read[TypeKey] and
      (JsPath \ "super_types").read[Seq[TypeKey]].map(_.toSet) and
      (JsPath \ "properties").read[Seq[PropKey]].map(_.toSet)
  )(NamedType.apply[TypeKey, PropKey] _)

}
