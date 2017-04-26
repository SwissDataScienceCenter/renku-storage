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

import javax.inject.{Inject, Singleton}
import scala.collection.JavaConversions._
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration
import org.pac4j.jwt.profile.JwtGenerator
import play.api.mvc.{Action, Controller}

/**
  * Created by jeberle on 25.04.17.
  */
@Singleton
class AccessController @Inject() (config: play.api.Configuration) extends Controller {

  def token = Action { implicit request =>
    val ssc = new SecretSignatureConfiguration(config.getString("serverSecret").get)
    val generator = new JwtGenerator(ssc)
    Ok(generator.generate(Map("sub"->"StorageService", "file_uuid" -> "uuid")))
  }

  def test_token = Action { implicit request =>
    Ok("ok")
  }

}
