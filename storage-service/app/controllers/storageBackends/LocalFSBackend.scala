package controllers.storageBackends

import java.io.{File, FileInputStream, FileNotFoundException, FileOutputStream}
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import play.api.libs.concurrent.ActorSystemProvider
import play.api.libs.concurrent.Execution.defaultContext
import play.api.libs.streams.Accumulator
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results.Created
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.matching.Regex

/**
  * Created by johann on 07/07/17.
  */
@Singleton
class LocalFSBackend @Inject()(config: play.api.Configuration, actorSystemProvider: ActorSystemProvider) extends Backend {

  private[this] val subConfig = config.getConfig("storage.backend.local").get
  val path: String = subConfig.getString("path").getOrElse(".")

  def read(request: RequestHeader, bucket: String, name: String): Option[Source[ByteString, _]] = {
    Try {
      val fullPath = s"$path/$bucket/$name"
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


  def write(req: RequestHeader, bucket: String, name: String): Accumulator[ByteString, Result] = {
    implicit val actorSystem: ActorSystem = actorSystemProvider.get
    implicit val mat: ActorMaterializer = ActorMaterializer()
    Accumulator.source[ByteString].mapFuture { source =>
      Future {
        val fullPath = s"$path/$bucket/$name"
        val os = new FileOutputStream(fullPath)
        val sink = StreamConverters.fromOutputStream(() => os)
        source.runWith(sink)
        Created
      }
    }
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

  def createBucket(request: RequestHeader, bucket: String): String = {
    new File(s"$path/$bucket").mkdir()
    bucket
  }

  private[this] def takeFromByteStringSource(source: Source[ByteString, _], n: Int, chunkSize: Int = 8192): Source[ByteString, _] = {
    source.mapConcat(identity).take(n).grouped(chunkSize).map{ bytes => ByteString(bytes: _*) }
  }

}
