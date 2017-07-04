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

package models

import ch.datascience.graph.elements.mutation.Mutation
import ch.datascience.graph.elements.mutation.json.MutationFormat
import ch.datascience.graph.elements.mutation.log.model.EventStatus
import ch.datascience.graph.elements.mutation.log.model.json.EventStatusFormat
import ch.datascience.graph.elements.persisted.PersistedVertex
import ch.datascience.graph.elements.persisted.json.PersistedVertexFormat
import play.api.libs.json._

/**
  * Created by johann on 25/04/17.
  */
package object json {

  implicit lazy val readResourceRequestReads: Reads[ReadResourceRequest] = ReadResourceRequestMappers.readResourceRequestReads
  implicit lazy val writeResourceRequestReads: Reads[WriteResourceRequest] = WriteResourceRequestMappers.writeResourceRequestReads
  implicit lazy val mutationFormat: Format[Mutation] = MutationFormat
  implicit lazy val vertexFormat: Format[PersistedVertex] = PersistedVertexFormat
  implicit lazy val eventStatusFormat: Format[EventStatus] = EventStatusFormat
}
