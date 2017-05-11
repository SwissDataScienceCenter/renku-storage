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

package controllers.scope

import javax.inject.Inject

import ch.datascience.graph.NamespaceAndName
import ch.datascience.graph.scope.PropertyScope
import ch.datascience.graph.types.{Cardinality, DataType, PropertyKey}
import injected.ScopeBean
import models.json.{CardinalityMappers, DataTypeMappers}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future
import scala.util.Try

/**
  * Created by johann on 11/05/17.
  */
class PropertyScopeController @Inject()(protected val scopeLayer: ScopeBean) extends Controller {

  protected val scope: PropertyScope[NamespaceAndName] = scopeLayer

//  implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

  def getCachedProperties: Action[Unit] = Action.async(BodyParsers.parse.empty) { implicit request =>
    scope.getCachedProperties map { properties =>
      val json = JsObject(
        for {
          (key, property) <- properties
        } yield key.asString -> Json.toJson(property)
      )
      Ok(json)
    }
  }

  def getPropertyFor(namespace: String, name: String): Action[Unit] = Action.async(BodyParsers.parse.empty) { implicit request =>
    val futureKey = Future.fromTry(Try( NamespaceAndName(namespace, name) ))
    val futureProperty = futureKey flatMap { key => scope.getPropertyFor(key) }
    futureProperty map {
      case Some(property) => Ok(Json.toJson(property))
      case None => NotFound
    } recover {
      case e: IllegalArgumentException => BadRequest(e.getMessage)
    }
  }


  def getPropertiesFor(keys: Set[NamespaceAndName]): Future[Map[NamespaceAndName, PropertyKey[NamespaceAndName]]] = scope.getPropertiesFor(keys)

  implicit lazy val propertyKeyWrites: Writes[PropertyKey[NamespaceAndName]] = (
    (JsPath \ "key").write[String] and
      (JsPath \ "cardinality").write[Cardinality](CardinalityMappers.cardinalityWrites) and
      (JsPath \ "dataType").write[DataType](DataTypeMappers.dataTypeWrites)
    )({ p: PropertyKey[NamespaceAndName] => (p.key.asString, p.cardinality, p.dataType) })

}
