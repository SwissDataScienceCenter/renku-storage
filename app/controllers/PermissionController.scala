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
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.security.spec.X509EncodedKeySpec
import java.security.{KeyFactory, KeyPair}
import java.util.Base64
import javax.inject.{Inject, Singleton}

import scala.collection.JavaConversions._
import org.pac4j.jwt.config.signature.RSASignatureConfiguration
import org.pac4j.jwt.profile.JwtGenerator
import play.api.mvc.{Action, BodyParsers, Controller}

/**
  * Created by jeberle on 25.04.17.
  */
@Singleton
class PermissionController @Inject()(config: play.api.Configuration) extends Controller {

  def authorize(id: Long) = Action.async(BodyParsers.parse.empty) { implicit request =>

    // get the graph element corresponding to the ID of the resource

    // validate its ACLs

    val public_key = Base64.getDecoder.decode(config.getString("publicKey").get)
    val private_key = Base64.getDecoder.decode(config.getString("privateKey").get)
    val public_spec = new X509EncodedKeySpec(public_key)
    val private_spec = new X509EncodedKeySpec(private_key)
    val kf = KeyFactory.getInstance("RSA")
    val key_pair = new KeyPair(kf.generatePublic(public_spec), kf.generatePrivate(private_spec))
    val signConfig = new RSASignatureConfiguration(key_pair)

    val generator = new JwtGenerator(signConfig)

    generator.generate(Map("sub"->"StorageService", "file_uuid" -> "uuid"))

    Ok()
  }

  def test_token = Action { implicit request =>
    Ok("ok")
  }

}
