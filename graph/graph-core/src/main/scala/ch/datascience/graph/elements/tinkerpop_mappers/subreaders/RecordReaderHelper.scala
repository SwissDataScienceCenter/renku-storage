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

import ch.datascience.graph.Constants
import ch.datascience.graph.elements.tinkerpop_mappers.KeyReader
import ch.datascience.graph.elements.tinkerpop_mappers.extracted.ExtractedProperty

import scala.util.Try

/**
  * Created by johann on 30/05/17.
  */
trait RecordReaderHelper {

  protected[this] def userPropertiesFilter[A <: ExtractedProperty](properties: Seq[A]): Seq[A] = {
    for {
      prop <- properties
      keyTry = Try { KeyReader.readSync(prop.key) }
      if keyTry.isSuccess
    } yield prop
  }

  protected[this] def typePropertiesFilter[A <: ExtractedProperty](properties: Seq[A]): Seq[A] = {
    for {
      prop <- properties
      if prop.key == Constants.TypeKey
    } yield prop
  }

}
