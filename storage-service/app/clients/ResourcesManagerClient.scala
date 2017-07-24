package clients

import javax.inject.Inject

import ch.datascience.service.models.resource.AccessGrant
import play.api.libs.json._
import play.api.libs.ws._
import ch.datascience.service.models.resource.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


class ResourcesManagerClient @Inject()(host: String)(implicit context: ExecutionContext, ws: WSClient) {

  def authorize[T](writer: Writes[T], rrequest: T)(implicit token: String): Future[Option[AccessGrant]] = {
    val request: WSRequest = ws.url(host + "/authorize")
      .withHeaders("Accept" -> "application/json", "Authorization" -> token)
      .withRequestTimeout(10000.millis)
    request.post(Json.toJson(rrequest)(writer)).map {
      response =>
        response.json.validate(AccessGrantFormat).asOpt
    }
  }
}

