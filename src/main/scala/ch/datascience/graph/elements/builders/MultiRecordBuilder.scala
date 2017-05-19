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

package ch.datascience.graph.elements.builders

import ch.datascience.graph.elements.{Property, MultiRecord}

/**
  * Created by johann on 19/05/17.
  */
trait MultiRecordBuilder[Key, Value, +Prop <: Property[Key, Value, Prop], +PB <: PropertyBuilder[Key, Value, Prop], +To <: MultiRecord[Key, Value, Prop]]
  extends Builder[To] {

  private[this] var myProperties: Map[Key, Seq[PB]] = Map.empty

  def properties: Map[Key, Seq[PB]] = myProperties

  def newProperty: Builder[PB]

  def +=(keyValue: (Key, Value)): this.type = {
    addProperty(keyValue._1, keyValue._2)
  }

  def addProperty(key: Key, value: Value): this.type = {
    val propertyBuilder = newProperty.result()
    propertyBuilder.key = key
    propertyBuilder.value = value
    val seq = properties get key match {
      case Some(s) => s :+ propertyBuilder
      case None => Seq(propertyBuilder)
    }
    myProperties += key -> seq
    this
  }

}
