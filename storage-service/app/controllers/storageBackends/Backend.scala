package controllers.storageBackends

import akka.stream.scaladsl.Source
import akka.util.ByteString
import play.api.mvc.RequestHeader

/**
  * Created by jeberle on 07.07.17.
  */
trait Backend {
  def read(request: RequestHeader, bucket: String, name: String): Option[Source[ByteString, _]]
  def write(request: RequestHeader, bucket: String, name: String, source: Source[ByteString, _]): Boolean
  def createBucket(request: RequestHeader, bucket: String): Boolean

}
