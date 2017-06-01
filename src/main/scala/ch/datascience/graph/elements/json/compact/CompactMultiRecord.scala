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

package ch.datascience.graph.elements.json.compact

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

/**
  * Created by johann on 01/06/17.
  */
object CompactMultiRecord {

  lazy val deflate: Reads[JsObject] = new Reads[JsObject] {

    def reads(json: JsValue): JsResult[JsObject] = {
      val jsObj = json.as[JsObject]
      val keys = (json \ "properties").as[JsObject].keys

      val res = keys.foldLeft[JsObject](jsObj) { (obj, key) =>
        obj.transform(deflateProperty(key)).get
      }

      JsSuccess(res)
    }

    private[this] def deflateProperty(key: String): Reads[JsObject] = (
      (JsPath \ "properties" \ key).json.prune and
        (JsPath \ "properties" \ key).read(CompactMultiPropertyValue.deflate).flatMap { prop =>
          (JsPath \ "properties" \ key).json.put(prop)
        }
    ).reduce

  }

  lazy val inflate: Reads[JsObject] = new Reads[JsObject] {

    def reads(json: JsValue): JsResult[JsObject] = {
      val jsObj = json.as[JsObject]
      val keys = (json \ "properties").as[JsObject].keys

      val res = keys.foldLeft[JsObject](jsObj) { (obj, key) =>
        obj.transform(inflateProperty(key)).get
      }

      JsSuccess(res)
    }

    private[this] def inflateProperty(key: String): Reads[JsObject] = (JsPath \ "properties" \ key).json.update(CompactMultiPropertyValue.inflate(key))

  }

}
