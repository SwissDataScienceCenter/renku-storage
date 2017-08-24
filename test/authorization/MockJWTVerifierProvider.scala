package authorization

import java.security.interfaces.RSAPublicKey
import javax.inject.{ Inject, Singleton }

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.{ JWT, JWTVerifier }
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

/**
 * Created by johann on 14/07/17.
 */
@Singleton
class MockJWTVerifierProvider @Inject() (
    keyPairProvider:  RSAKeyPairProvider,
    configuration:    Configuration,
    wSClient:         WSClient,
    executionContext: ExecutionContext
) extends JWTVerifierProvider( configuration, wSClient, executionContext ) {

  override def get: JWTVerifier = verifier

  private[this] lazy val verifier: JWTVerifier = {
    val publicKey: RSAPublicKey = keyPairProvider.getPublicKey
    val algorithm = Algorithm.RSA256( publicKey, null )
    JWT.require( algorithm ).build()
  }

}
