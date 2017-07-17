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

package authorization

import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.time.Instant
import javax.inject.{Inject, Singleton}

import ch.datascience.service.security.{PrivateKeyReader, PublicKeyReader}
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import play.api.Configuration

/**
  * Created by johann on 17/07/17.
  */
@Singleton
class TokenSignerProvider @Inject() (configuration: Configuration) {

  def get: Algorithm = algorithm

  def addDefaultHeadersAndClaims(builder: JWTCreator.Builder): JWTCreator.Builder = {
    builder.withIssuer("resources-manager").withIssuedAt(java.util.Date.from(Instant.now()))

    ???
  }

  private[this] lazy val algorithm: Algorithm = {
    val publicKey: RSAPublicKey = PublicKeyReader.readRSAPublicKey(configuration.getString("key.resource-manager.public").get)
    val privateKey: RSAPrivateKey = PrivateKeyReader.readRSAPrivateKey(configuration.getString("key.resource-manager.private").get)
    Algorithm.RSA256(publicKey, privateKey)
  }

}
