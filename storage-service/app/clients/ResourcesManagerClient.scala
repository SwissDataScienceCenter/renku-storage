package clients

import javax.inject.Inject

import play.api.libs.json._
import play.api.libs.ws._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ResourcesManagerClient @Inject()(implicit context: ExecutionContext, ws: WSClient, host: String, token: String) {

  def authorize[T](writer: Writes[T], rrequest: T): Future[JsValue] = {
    val request: WSRequest = ws.url(host + "/authorize")
      .withHeaders("Accept" -> "application/json", "Authorization" -> token)
      .withRequestTimeout(10000.millis)
    val futureResult: Future[JsValue] = request.post(Json.toJson(rrequest)(writer)).map {
      response =>
        response.json
    }
    futureResult
  }
}

