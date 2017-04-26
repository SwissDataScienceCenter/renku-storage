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

package controllers

import javax.inject.Inject

import ch.datascience.typesystem.model.GraphObject
import injected.OrchestrationLayer
import models.json._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._

import scala.util.{Failure, Success}

/**
  * Created by johann on 26/04/17.
  */
class ValidationController @Inject()(protected val orchestrator: OrchestrationLayer) extends Controller with JsonComponent {

  implicit val builder = new ((Set[String], Map[String, Any]) => GraphObject) {
    def apply(types: Set[String], properties: Map[String, Any]): GraphObject = GraphObject(types, properties)
  }

  def validator: Action[Unit] = Action.async(BodyParsers.parse.empty) { implicit request =>
    orchestrator.getCurrentValidator map { validator =>
      Ok(Json.toJson(validator.propertyKeys))
    }
  }

  def validateObject: Action[GraphObject] = Action.async(bodyParseJson[GraphObject]) { implicit request =>
    val graphObject = request.body
    orchestrator.getCurrentValidator map { validator =>
      validator.validateObject(graphObject) match {
        case Success(validated) => Ok(JsObject(Seq(("result", JsBoolean(true)), ("object", Json.toJson(validated)))))
        case Failure(e) => Ok(JsObject(Seq(("result", JsBoolean(false)), ("error", JsString(e.getMessage)))))
      }
    }
  }

}
