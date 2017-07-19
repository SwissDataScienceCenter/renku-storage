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

import java.security.interfaces.RSAPublicKey
import javax.inject.{Inject, Singleton}

import ch.datascience.service.security.PublicKeyReader
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.{JWT, JWTVerifier}
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

/**
  * Created by johann on 14/07/17.
  */
@Singleton
class JWTVerifierProvider @Inject() (configuration: Configuration, wSClient: WSClient, executionContext: ExecutionContext) {

  def get: JWTVerifier = verifier

  private[this] lazy val verifier: JWTVerifier = {
    val publicKey: RSAPublicKey = getPublicKey
    val algorithm = Algorithm.RSA256(publicKey, null)
    JWT.require(algorithm).build()
  }

  private[this] def getPublicKey: RSAPublicKey = {
    implicit val ws: WSClient = wSClient
    implicit val ec: ExecutionContext = executionContext
    val futureKey = PublicKeyReader.getRSAPublicKey(configuration.getConfig("identity-manager.key").get)
    Await.result(futureKey, 2.minutes)
  }

}
