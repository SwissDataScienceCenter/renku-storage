package controllers

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
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.PlayException
import play.api.libs.streams._
import play.api.mvc._

import scala.collection.JavaConversions.asScalaBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Try}
import scala.util.matching.Regex


@Singleton
class SwiftController @Inject()(config: play.api.Configuration, val playSessionStore: PlaySessionStore) extends Controller {

  private def getProfiles(implicit request: RequestHeader): List[CommonProfile] = {
    val webContext = new PlayWebContext(request, playSessionStore)
    val profileManager = new ProfileManager[CommonProfile](webContext)
    val profiles = profileManager.getAll(true)
    asScalaBuffer(profiles).toList
  }

  val swiftConfig = new AccountConfig()
  swiftConfig.setUsername(config.getString("swift.username").get)
  swiftConfig.setPassword(config.getString("swift.password").get)
  swiftConfig.setAuthUrl(config.getString("swift.auth_url").get)
  swiftConfig.setTenantId(config.getString("swift.project").get)
  val swiftAccount: Account = new AccountFactory(swiftConfig).createAccount()

  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r

  def read_object(name: String) = Action.async { implicit request =>
    val profile = getProfiles(request).head
    val bucket = request.headers.get("container").getOrElse(profile.getId)
    read(implicitly, bucket, name)
  }

  def read_object = Action.async { implicit request =>
    val profile = getProfiles(request).head
    val bucket = Try(profile.getAttribute("bucket").toString)
    val name = Try(profile.getAttribute("name").toString)
    val scope = Try(profile.getAttribute("scope").toString)
    val m = scope.flatMap(s => if (s.equalsIgnoreCase("storage:read")) name.flatMap(n => bucket.map(b =>
      read(implicitly, b, n))) else Failure(new PlayException("Forbidden", "Wrong scope")))
    m.getOrElse(Future(Forbidden("The token is missing the required permissions")))
  }

  def read(implicit request: RequestHeader, bucket: String, name: String): Future[Result] =
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

  def write_object = Action(forward()) { request =>
    request.body
  }

  def forward(): BodyParser[Result] = BodyParser { req =>
    Accumulator.source[ByteString].mapFuture { source =>
      Future {
        val profile = getProfiles(req).head
        val bucket = Try(profile.getAttribute("bucket").toString)
        val name = Try(profile.getAttribute("name").toString)
        val scope = Try(profile.getAttribute("scope").toString)
        val m = scope.flatMap(s => if (s.equalsIgnoreCase("storage:write")) name.flatMap(n => bucket.map(b => {
          implicit val system = ActorSystem("Sys")
          implicit val materializer = ActorMaterializer()
          val container = swiftAccount.getContainer(b)
          if (!container.exists()) container.create()
          val obj = container.getObject(n)
          val inputStream = source.runWith(
            StreamConverters.asInputStream(FiniteDuration(3, TimeUnit.SECONDS))
          )
          obj.uploadObject(inputStream)
          Right(Created(""))
        }))
        else Failure(new PlayException("Forbidden", "Wrong scope")))
        m.getOrElse(Right(Forbidden("The token is missing the required permissions")))
      }
    }
  }


  def write_object(name: String) = Action(forward(name)) { request =>
    request.body
  }

  def forward(name: String): BodyParser[Result] = BodyParser { req =>
    Accumulator.source[ByteString].mapFuture { source =>
      Future {
        val profile = getProfiles(req).head
        val bucket = req.headers.get("container").getOrElse(profile.getId)
        implicit val system = ActorSystem("Sys")
        implicit val materializer = ActorMaterializer()
        val container = swiftAccount.getContainer(bucket)
        if (!container.exists()) container.create()
        val obj = container.getObject(name)
        val inputStream = source.runWith(
          StreamConverters.asInputStream(FiniteDuration(3, TimeUnit.SECONDS))
        )
        obj.uploadObject(inputStream)
        Right(Created(""))
      }
    }
  }
}