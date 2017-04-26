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
