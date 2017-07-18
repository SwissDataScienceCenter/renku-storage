package controllers.storageBackends

import akka.stream.scaladsl.Source
import akka.util.ByteString
import play.api.libs.streams.Accumulator
import play.api.mvc.{RequestHeader, Result}

/**
  * Created by jeberle on 07.07.17.
  */
trait Backend {
  def read(request: RequestHeader, bucket: String, name: String): Option[Source[ByteString, _]]
  def write(request: RequestHeader, bucket: String, name: String):  Accumulator[ByteString, Result]
  def createBucket(request: RequestHeader, bucket: String): Boolean

}
