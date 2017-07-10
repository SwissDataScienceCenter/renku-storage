package models.json

import models.{CreateBucketRequest, ReadResourceRequest, WriteResourceRequest}
import play.api.libs.functional.syntax._
import play.api.libs.json._


object ResourceRequestMappers {

  def writeResourceRequestReads: Reads[WriteResourceRequest] = (
      (JsPath \ "app_id").readNullable[Long] and
      (JsPath \ "bucket").read[Long] and
      (JsPath \ "target").read[Either[String, Long]]
    )(WriteResourceRequest.apply _)

  private[this] implicit lazy val targetReader: Reads[Either[String, Long]] = (JsPath \ "type").read[String].flatMap {
    case "filename" => (JsPath \ "filename").read[String].map(s => Left(s))
    case "resource" => (JsPath \ "resource_id").read[Long].map(l => Right(l))
    case t => Reads { json => JsError(s"Usupported type $t") }
  }

  def writeResourceRequestWrites: Writes[WriteResourceRequest] = (
    (JsPath \ "app_id").writeNullable[Long] and
      (JsPath \ "bucket").write[Long] and
      (JsPath \ "target").write[Either[String, Long]]
    ) { wr => (wr.appId, wr.bucket, wr.target) }

  private[this] implicit lazy val targetWriter: Writes[Either[String, Long]] = (
    (JsPath \ "type").write[String] and
    (JsPath \ "filename").writeNullable[String] and
    (JsPath \ "resource_id").writeNullable[Long]
    ) { wr => wr match {
      case Left(filename) => ("filename", Some(filename), None)
      case Right(id) => ("resource", None, Some(id))
    }}

  def createBucketRequestReads: Reads[CreateBucketRequest] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "backend").read[String]
  )(CreateBucketRequest.apply _)

  def createBucketRequestWrites: Writes[CreateBucketRequest] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "backend").write[String]
    ) { cb => (cb.name, cb.backend) }

  def readResourceRequestReads: Reads[ReadResourceRequest] = (
    (JsPath \ "app_id").readNullable[Long] and
      (JsPath \ "resource_id").read[Long]
    )(ReadResourceRequest.apply _)

  def readResourceRequestWrites: Writes[ReadResourceRequest] = (
    (JsPath \ "app_id").writeNullable[Long] and
      (JsPath \ "resource_id").write[Long]
    ) { rr => (rr.appId, rr.resourceId) }
}
