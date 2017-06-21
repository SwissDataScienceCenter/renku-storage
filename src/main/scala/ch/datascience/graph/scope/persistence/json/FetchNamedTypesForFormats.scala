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

import ch.datascience.graph.naming.json.NamespaceAndNameFormat
import ch.datascience.graph.types.NamedType
import ch.datascience.graph.types.json.NamedTypeFormat
import play.api.libs.json._

/**
  * Created by johann on 19/06/17.
  */
object FetchNamedTypesForFormats {

  object QueryFormat extends Format[Set[NamedType#TypeId]] {

    def writes(keys: Set[NamedType#TypeId]): JsValue = seqWrites.writes(keys.toSeq)

    def reads(json: JsValue): JsResult[Set[NamedType#TypeId]] = for { seq <- seqReads.reads(json) } yield seq.toSet

    private[this] lazy val seqWrites = Writes.seq(NamespaceAndNameFormat)

    private[this] lazy val seqReads = Reads.seq(NamespaceAndNameFormat)

  }

  object ResponseFormat extends Format[Map[NamedType#TypeId, NamedType]] {

    def writes(definitions: Map[NamedType#TypeId, NamedType]): JsValue = seqWrites.writes(definitions.values)

    def reads(json: JsValue): JsResult[Map[NamedType#TypeId, NamedType]] = seqReads.reads(json) map { seq =>
      val withKey = for {
        namedType <- seq
      } yield namedType.typeId -> namedType
      withKey.toMap
    }

    private[this] lazy val seqWrites = Writes.traversableWrites(NamedTypeFormat)

    private[this] lazy val seqReads = Reads.seq(NamedTypeFormat)

  }

}
