package controllers

import javax.inject._

import authorization.ResourcesManagerJWTVerifierProvider
import ch.datascience.service.security.{ProfileFilterAction, TokenFilter}
import controllers.storageBackends.Backends
import play.api.libs.streams._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex


@Singleton
class IOController @Inject()(config: play.api.Configuration, backends: Backends, jwtVerifier: ResourcesManagerJWTVerifierProvider) extends Controller {


  val RangePattern: Regex = """bytes=(\d+)?-(\d+)?.*""".r


  def objectRead = ProfileFilterAction(jwtVerifier.get).async { implicit request =>
    Future {
      val bucket = request.token.getClaim("bucket").asString()
      val name = request.token.getClaim("name").asString()
      val backend = request.token.getClaim("backend").asString()

      backends.getBackend(backend) match {
        case Some(back) =>
              back.read(request, bucket, name) match {
                case Some(dataContent) => Ok.chunked(dataContent)
                case None => NotFound
              }
        case None => BadRequest(s"The backend $backend is not enabled.")
      }
    }
  }

  def objectWrite = EssentialAction { reqh =>
        TokenFilter(jwtVerifier.get, "").filter(reqh) match {
          case Right(profile) =>
            val bucket = profile.getClaim("bucket").asString()
            val name = profile.getClaim("name").asString()
            val backend = profile.getClaim("backend").asString()
            backends.getBackend(backend) match {
              case Some(back) =>
                  back.write(reqh, bucket, name)
              case None => Accumulator.done(BadRequest(s"The backend $backend is not enabled."))
            }
          case Left(res) => Accumulator.done(res)
        }
      }
}