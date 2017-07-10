package modules

import com.google.inject.AbstractModule
import org.pac4j.core.client.Clients
import play.api.{Configuration, Environment}
import org.pac4j.play.store.{PlayCacheSessionStore, PlaySessionStore}
import org.pac4j.core.config.Config
import org.pac4j.http.client.direct.HeaderClient
import org.pac4j.jwt.config.signature.RSASignatureConfiguration
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import java.security.{KeyFactory, KeyPair}
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

import controllers.security.{HttpActionAdapter, ServerHeaderClient, StorageManagerAuthorizer, UserTokenAuthorizer}


class SecurityModule(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {

    val user_jwtAuthenticator = new JwtAuthenticator()
    val user_key = Base64.getDecoder.decode(configuration.getString("key.keycloak.public").get)
    val user_spec = new X509EncodedKeySpec(user_key)
    val kf = KeyFactory.getInstance("RSA")
    val user_pair = new KeyPair(kf.generatePublic(user_spec), null)
    user_jwtAuthenticator.addSignatureConfiguration(new RSASignatureConfiguration(user_pair))
    val user_headerClient = new HeaderClient("Authorization", "Bearer ", user_jwtAuthenticator)

    val server_jwtAuthenticator = new JwtAuthenticator()
    val server_key = Base64.getDecoder.decode(configuration.getString("key.resource-manager.public").get)
    val server_spec = new X509EncodedKeySpec(server_key)
    val server_pair = new KeyPair(kf.generatePublic(server_spec), null)
    server_jwtAuthenticator.addSignatureConfiguration(new RSASignatureConfiguration(server_pair))
    val server_headerClient = new ServerHeaderClient("Authorization", "Bearer ", server_jwtAuthenticator)

    val clients = new Clients(server_headerClient, user_headerClient)

    val config = new Config(clients)
    config.addAuthorizer("storage_manager", new StorageManagerAuthorizer())
    config.addAuthorizer("user_token", new UserTokenAuthorizer())
    config.setHttpActionAdapter(new HttpActionAdapter())
    bind(classOf[Config]).toInstance(config)

    bind(classOf[PlaySessionStore]).to(classOf[PlayCacheSessionStore])

  }
}