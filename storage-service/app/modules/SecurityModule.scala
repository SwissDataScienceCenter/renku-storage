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

package modules

import com.google.inject.AbstractModule
import controllers.{CustomAuthorizer, HttpActionAdapter, RoleAdminAuthGenerator}
import org.pac4j.core.client.Clients
import org.pac4j.oidc.client.OidcClient
import play.api.{Configuration, Environment}
import org.pac4j.play.store.{PlayCacheSessionStore, PlaySessionStore}
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.client.direct.AnonymousClient
import org.pac4j.core.config.Config
import org.pac4j.http.client.direct.ParameterClient
import org.pac4j.jwt.config.signature.{RSASignatureConfiguration, SecretSignatureConfiguration}
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import java.security.{KeyFactory, KeyPair}
import java.security.spec.{PKCS8EncodedKeySpec, RSAPublicKeySpec, X509EncodedKeySpec}
import java.util.Base64


class SecurityModule(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {

    /*val oidcConfiguration = new OidcConfiguration()
    oidcConfiguration.setClientId("storage-service")
    oidcConfiguration.setSecret("80183a19-6e47-42ac-aaa9-683726de857e")
    oidcConfiguration.setDiscoveryURI("https://internal.datascience.ch:8089/auth/realms/SDSC/.well-known/openid-configuration")
    val oidcClient = new OidcClient[OidcProfile](oidcConfiguration)
    oidcClient.addAuthorizationGenerator(new RoleAdminAuthGenerator)
   */

    val jwtAuthenticator = new JwtAuthenticator()
    val key = Base64.getDecoder.decode(configuration.getString("publicKey").get)
    val spec = new X509EncodedKeySpec(key)
    val kf = KeyFactory.getInstance("RSA")
    val pair = new KeyPair(kf.generatePublic(spec), null)
    jwtAuthenticator.addSignatureConfiguration(new RSASignatureConfiguration(pair))
    val parameterClient = new ParameterClient("token", jwtAuthenticator)
    parameterClient.setSupportGetRequest(true)
    parameterClient.setSupportPostRequest(false)


    val clients = new Clients("http://localhost:9000/callback", parameterClient)

    val config = new Config(clients)
    config.addAuthorizer("admin", new CustomAuthorizer())
    config.setHttpActionAdapter(new HttpActionAdapter())
    bind(classOf[Config]).toInstance(config)

    bind(classOf[PlaySessionStore]).to(classOf[PlayCacheSessionStore])

  }
}