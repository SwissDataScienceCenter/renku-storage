package controllers

import java.util.concurrent.TimeUnit
import javax.inject._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import io.minio.MinioClient
import org.javaswift.joss.client.factory.{AccountConfig, AccountFactory}
import org.javaswift.joss.headers.`object`.range.{FirstPartRange, LastPartRange, MidPartRange}
import org.javaswift.joss.instructions.DownloadInstructions
import org.javaswift.joss.model.Account
import play.api.mvc._
import play.api.libs.streams._

import scala.util.matching.Regex
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration


@Singleton
class ProxyController @Inject() (config: play.api.Configuration) extends Controller {

  // Create a minioClient with the Minio Server name, Port, Access key and Secret key.
  //val minioClient = new MinioClient("https://internal.datascience.ch:9000", "SDSCSDSC", "SDSC2017")
  //val minioClient = new MinioClient("https://os.zhdk.cloud.switch.ch", "7612f637a809455b8958c4b3c063ca9b", "8afb798a4c354b0ea2b3160f9c7fcd47")

  val swiftConfig = new AccountConfig()
  swiftConfig.setUsername(config.getString("swift.username").get)
  swiftConfig.setPassword(config.getString("swift.password").get)
  swiftConfig.setAuthUrl(config.getString("swift.auth_url").get)
  swiftConfig.setTenantId(config.getString("swift.project").get)
  val swiftAccount: Account = new AccountFactory(swiftConfig).createAccount()

  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r

  def read_object = Action { implicit request =>
    val bucket = request.getQueryString("bucket")
    val name = request.getQueryString("name")
    val CHUNK_SIZE = 100
    val container = swiftAccount.getContainer(bucket.get)
    val obj = container.getObject(name.get)
    val instructions = new DownloadInstructions()
    request.headers.get("Range").map {
      case RangePattern(null, to) => instructions.setRange(new FirstPartRange(to.toInt))
      case RangePattern(from, null) => instructions.setRange(new LastPartRange(from.toInt))
      case RangePattern(from, to) => instructions.setRange(new MidPartRange(from.toInt, to.toInt))
      case _ =>
    }
    val data = obj.downloadObjectAsInputStream(instructions)
    //val data = minioClient.getObject(bucket.get, name.get)
    val dataContent: Source[ByteString, _] = StreamConverters.fromInputStream(() => data, CHUNK_SIZE)

    Ok.chunked(dataContent)
  }


  def forward: BodyParser[Result] = BodyParser { req =>
    Accumulator.source[ByteString].mapFuture { source =>
      val size = req.headers.get("Content-Length")
      implicit val system = ActorSystem("Sys")
      implicit val materializer = ActorMaterializer()
      val bucket = req.getQueryString("bucket")
      val name = req.getQueryString("name")
      val container = swiftAccount.getContainer(bucket.get)
      if (!container.exists()) container.create()
      val obj = container.getObject(name.get)
      //val isExist = minioClient.bucketExists(bucket.get)
      //if (!isExist) minioClient.makeBucket(bucket.get)
      val inputStream = source.runWith(
        StreamConverters.asInputStream(FiniteDuration(3, TimeUnit.SECONDS))
      )
      obj.uploadObject(inputStream)

      //minioClient.putObject(bucket.get, name.get, inputStream, size.get.toLong, "application/octet-stream")

      Future(Right(Ok("ok")))
    }
  }

  def write_object = Action(forward) { request =>
    request.body
  }
}