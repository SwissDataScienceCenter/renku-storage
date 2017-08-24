package authorization

import java.security.interfaces.{ RSAPrivateKey, RSAPublicKey }
import java.security.{ KeyPair, KeyPairGenerator }
import javax.inject.Singleton

@Singleton
class RSAKeyPairProvider {

  def get: KeyPair = keyPair

  def getPublicKey: RSAPublicKey = keyPair.getPublic.asInstanceOf[RSAPublicKey]

  def getPrivateKey: RSAPrivateKey = keyPair.getPrivate.asInstanceOf[RSAPrivateKey]

  private[this] lazy val keyPair: KeyPair = makeKeyPair

  private[this] def makeKeyPair: KeyPair = {
    val keyGen = KeyPairGenerator.getInstance( "RSA" )
    keyGen.initialize( 1024 )
    keyGen.generateKeyPair()
  }

}
