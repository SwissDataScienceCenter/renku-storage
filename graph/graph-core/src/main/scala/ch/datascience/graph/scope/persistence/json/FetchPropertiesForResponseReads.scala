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

package ch.datascience.graph.scope.persistence.json

import ch.datascience.graph.types.PropertyKey
import ch.datascience.graph.types.json.PropertyKeyFormat
import play.api.libs.json.{JsResult, JsValue, Reads}

/**
  * Created by johann on 24/05/17.
  */
object FetchPropertiesForResponseReads extends Reads[Map[PropertyKey#Key, PropertyKey]] {

  def reads(json: JsValue): JsResult[Map[PropertyKey#Key, PropertyKey]] = seqReads.reads(json) map { seq =>
    val withKey = for {
      property <- seq
    } yield property.key -> property
    withKey.toMap
  }

  private[this] lazy val seqReads = Reads.seq(PropertyKeyFormat)

}
