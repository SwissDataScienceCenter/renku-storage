package controllers.storageBackends

import java.io.{File, FileInputStream, FileNotFoundException, FileOutputStream}
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import play.api.libs.concurrent.ActorSystemProvider
import play.api.libs.concurrent.Execution.defaultContext
import play.api.mvc.RequestHeader

import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.util.matching.Regex

/**
  * Created by johann on 07/07/17.
  */
@Singleton
class LocalFSBackend @Inject()(actorSystemProvider: ActorSystemProvider) extends Backend {

  def read(request: RequestHeader, bucket: String, name: String): Option[Source[ByteString, _]] = {
    Try {
      val fullPath = s"$bucket/$name"
      val (from, to) = getRange(request)

      val is = new FileInputStream(fullPath)
      for (n <- from) {
        is.skip(n)
      }

      val dataContent: Source[ByteString, _] = StreamConverters.fromInputStream(() => is)
      val dataContent2: Source[ByteString, _] = (from, to) match {
        case (Some(n), Some(m)) =>
          takeFromByteStringSource(dataContent, m - n)
        case (None, Some(m)) =>
          takeFromByteStringSource(dataContent, m)
        case _ => dataContent
      }

      Some(dataContent2)
    }.recover {
      case _: FileNotFoundException | _: SecurityException => None
    }.get
  }


  def write(req: RequestHeader, bucket: String, name: String, source: Source[ByteString, _]): Boolean = {
    implicit val actorSystem: ActorSystem  = actorSystemProvider.get
    implicit val mat: ActorMaterializer = ActorMaterializer()

    val fullPath = s"$bucket/$name"
    val os = new FileOutputStream(fullPath)
    val sink = StreamConverters.fromOutputStream(() => os)
    source.runWith(sink)
    true
  }

  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r

  def getRange(request: RequestHeader): (Option[Int], Option[Int]) = {
    val opt = request.headers.get("Range").map {
      case RangePattern(null, to) => (None, Some(to.toInt))
      case RangePattern(from, null) => (Some(from.toInt), None)
      case RangePattern(from, to) => (Some(from.toInt), Some(to.toInt))
      case _ => (None, None)
    }
    opt.getOrElse((None, None))
  }

  private[this] implicit lazy val ex: ExecutionContext = defaultContext

  def createBucket(request: RequestHeader, bucket: String): Boolean = {
    new File(bucket).mkdir()
  }

  private[this] def takeFromByteStringSource(source: Source[ByteString, _], n: Int, chunkSize: Int = 8192): Source[ByteString, _] = {
    source.mapConcat(identity).take(n).grouped(chunkSize).map{ bytes => ByteString(bytes: _*) }
  }

}
