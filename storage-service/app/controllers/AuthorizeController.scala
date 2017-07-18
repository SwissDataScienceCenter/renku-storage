package controllers

import javax.inject.{Inject, Singleton}

import authorization.JWTVerifierProvider
import ch.datascience.service.models.resource.{CreateBucketRequest, ReadResourceRequest, WriteResourceRequest}
import ch.datascience.service.security.ProfileFilterAction
import ch.datascience.service.utils.ControllerWithBodyParseJson
import ch.datascience.service.models.resource.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import clients.ResourcesManagerClient

import scala.concurrent.Future

/**
  * Created by jeberle on 25.04.17.
  */
@Singleton
class AuthorizeController @Inject()(config: play.api.Configuration,
                                    jwtVerifier: JWTVerifierProvider,
                                    implicit val wsclient: WSClient
                                    ) extends Controller with ControllerWithBodyParseJson with RequestHelper{

  lazy val host: String = config
    .getString("resources.manager.service.host")
    .getOrElse("http://localhost:9000/api/resources/")

  def objectRead = ProfileFilterAction(jwtVerifier.get).async(bodyParseJson[ReadResourceRequest](ReadResourceRequestFormat)) { implicit request =>
    Future {
      implicit val token: String = request.headers.get("Authorization").getOrElse("")
      val rrr = request.body
      val rmc = new ResourcesManagerClient(host)
      rmc.authorize(ReadResourceRequestFormat, rrr)

      // TODO Check permission and forward token

      Ok
    }
  }

  def objectWrite = ProfileFilterAction(jwtVerifier.get).async(bodyParseJson[WriteResourceRequest](WriteResourceRequestFormat)) { implicit request =>
    Future {
      implicit val token: String = request.headers.get("Authorization").getOrElse("")
      val wrr = request.body
      val rmc = new ResourcesManagerClient(host)
      rmc.authorize(WriteResourceRequestFormat, wrr)

      // TODO Check permission and forward token

      Ok
    }
  }

  def bucketCreate = ProfileFilterAction(jwtVerifier.get).async(bodyParseJson[CreateBucketRequest](CreateBucketRequestFormat)) { implicit request =>
    Future{
      implicit val token: String = request.headers.get("Authorization").getOrElse("")
      val cbr = request.body
      val rmc = new ResourcesManagerClient(host)
      rmc.authorize(CreateBucketRequestFormat, cbr)

  // TODO Check permission and forward token

  Ok
    }

  }
}
