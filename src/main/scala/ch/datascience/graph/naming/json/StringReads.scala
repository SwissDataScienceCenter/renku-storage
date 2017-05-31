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

package ch.datascience.graph.naming.json

import play.api.libs.json._

/**
  * Created by johann on 24/05/17.
  */
trait StringReads[Key] extends Reads[Key] { self =>

  final def reads(json: JsValue): JsResult[Key] = implicitly[Reads[String]].reads(json).flatMap(str => reads(JsString(str)))

  def reads(jsString: JsString): JsResult[Key]

  def mapReads[Value : Reads]: Reads[Map[Key, Value]] = new Reads[Map[Key, Value]] {
    def reads(json: JsValue): JsResult[Map[Key, Value]] = for {
      map <- implicitly[Reads[Map[String, Value]]].reads(json)
      tMap <- transform(map)
    } yield tMap
  }

  private[this] def transform[Value](map: Map[String, Value]): JsResult[Map[Key, Value]] = for {
    keysMap <- Reads.map[Key](self).reads( jsonKeyMap(map.keys) )
  } yield for {
    (stringKey, value) <- map
  } yield keysMap(stringKey) -> value

  private[this] def jsonKeyMap(stringKeys: Iterable[String]): JsObject = {
    val map = for {
      str <- stringKeys
    } yield str -> JsString(str)

    JsObject(map.toMap)
  }

}
