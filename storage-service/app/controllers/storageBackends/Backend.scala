package controllers.storageBackends

import akka.stream.scaladsl.Source
import akka.util.ByteString
import play.api.mvc.{Controller, RequestHeader, Result}
import scala.concurrent.Future

/**
  * Created by jeberle on 07.07.17.
  */
trait Backend { this: Controller =>

  def read(request: RequestHeader, bucket: String, name: String): Future[Result]
  def write(req: RequestHeader, bucket: String, name: String, source: Source[ByteString, _]): Result

}
