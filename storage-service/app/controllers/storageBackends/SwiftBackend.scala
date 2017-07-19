package controllers.storageBackends

import java.util.concurrent.TimeUnit
import javax.inject._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import org.javaswift.joss.client.factory.{AccountConfig, AccountFactory}
import org.javaswift.joss.headers.`object`.range.{FirstPartRange, LastPartRange, MidPartRange}
import org.javaswift.joss.instructions.DownloadInstructions
import org.javaswift.joss.model.Account
import play.api.libs.concurrent.ActorSystemProvider

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.streams.Accumulator
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.matching.Regex


@Singleton
class SwiftBackend @Inject()(config: play.api.Configuration, actorSystemProvider: ActorSystemProvider) extends Backend {

  val swiftConfig = new AccountConfig()
  swiftConfig.setUsername(config.getString("swift.username").get)
  swiftConfig.setPassword(config.getString("swift.password").get)
  swiftConfig.setAuthUrl(config.getString("swift.auth_url").get)
  swiftConfig.setTenantId(config.getString("swift.project").get)
  lazy val swiftAccount: Account = new AccountFactory(swiftConfig).createAccount()

  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r


  def read(request: RequestHeader, bucket: String, name: String): Option[Source[ByteString, _]] = {
    val CHUNK_SIZE = 100
    val container = swiftAccount.getContainer(bucket)
    if (container.exists() && container.getObject(name).exists()) {
      val instructions = new DownloadInstructions()
      request.headers.get("Range").map {
        case RangePattern(null, to) => instructions.setRange(new FirstPartRange(to.toInt))
        case RangePattern(from, null) => instructions.setRange(new LastPartRange(from.toInt))
        case RangePattern(from, to) => instructions.setRange(new MidPartRange(from.toInt, to.toInt))
        case _ =>
      }
      val data = container.getObject(name).downloadObjectAsInputStream(instructions)
      Some(StreamConverters.fromInputStream(() => data, CHUNK_SIZE))
    } else {
      None
    }
  }

  def write(req: RequestHeader, bucket: String, name: String): Accumulator[ByteString, Result] = {

    implicit val actorSystem: ActorSystem  = actorSystemProvider.get
    implicit val mat: ActorMaterializer = ActorMaterializer()
    val container = swiftAccount.getContainer(bucket)
    if (container.exists())
      Accumulator.source[ByteString].mapFuture { source =>
        Future {
          val obj = container.getObject(name)
          val inputStream = source.runWith(
            StreamConverters.asInputStream(FiniteDuration(3, TimeUnit.SECONDS))
          )
          obj.uploadObject(inputStream)
          inputStream.close()
          Created
        }
    }
    else
      Accumulator.done(NotFound)
  }

  def createBucket(request: RequestHeader, bucket: String): String = {
    val uuid = java.util.UUID.randomUUID.toString
    val container = swiftAccount.getContainer(uuid)
    container.create()
    uuid
  }
}