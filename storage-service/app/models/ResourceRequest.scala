package models

/**
  * Created by jeberle on 09.06.17.
  */

case class ReadResourceRequest(appId: Option[Long], resourceId: Long)
case class WriteResourceRequest(appId: Option[Long], bucket: Long ,target: Either[String, Long])
case class CreateBucketRequest(name: String, backend: String)