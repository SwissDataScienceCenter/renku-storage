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

package ch.datascience.graph.elements.tinkerpop_mappers.subreaders

import ch.datascience.graph.elements.persisted.{Path, PersistedRecordProperty}
import ch.datascience.graph.elements.tinkerpop_mappers.extracted.ExtractedLeafProperty
import ch.datascience.graph.elements.tinkerpop_mappers.{KeyReader, Reader, ValueReader}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by johann on 30/05/17.
  */
case class LeafPropertyReader(valueReader: ValueReader) extends Reader[(Path, ExtractedLeafProperty), PersistedRecordProperty] {

  def read(t: (Path, ExtractedLeafProperty))(implicit ec: ExecutionContext): Future[PersistedRecordProperty] = {
    val (parent, prop) = t

    for {
      key <- KeyReader.read(prop.key)
      value <- valueReader.read((key, prop.value))
    } yield PersistedRecordProperty(parent, key, value)
  }

}
