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

import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.matching.Regex


@Singleton
class SwiftBackend @Inject()(config: play.api.Configuration) extends Controller with Backend {

  val swiftConfig = new AccountConfig()
  swiftConfig.setUsername(config.getString("swift.username").get)
  swiftConfig.setPassword(config.getString("swift.password").get)
  swiftConfig.setAuthUrl(config.getString("swift.auth_url").get)
  swiftConfig.setTenantId(config.getString("swift.project").get)
  val swiftAccount: Account = new AccountFactory(swiftConfig).createAccount()

  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r


  def read(request: RequestHeader, bucket: String, name: String): Future[Result] =
    Future {
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
        val dataContent: Source[ByteString, _] = StreamConverters.fromInputStream(() => data, CHUNK_SIZE)

        Ok.chunked(dataContent)
      } else {
        NotFound("File not found")
      }
  }

  def write(req: RequestHeader, bucket: String, name: String, source: Source[ByteString, _]): Result = {
    implicit val system = ActorSystem("Sys")
    implicit val materializer = ActorMaterializer()
    val container = swiftAccount.getContainer(bucket)
    if (!container.exists())
      NotFound
    else {
      val obj = container.getObject(name)
      val inputStream = source.runWith(
        StreamConverters.asInputStream(FiniteDuration(3, TimeUnit.SECONDS))
      )
      obj.uploadObject(inputStream)
      Created
    }
  }
}