package controllers

import authorization.{ JWTVerifierProvider, MockJWTVerifierProvider }
import ch.datascience.service.utils.persistence.graph.JanusGraphProvider
import ch.datascience.test.utils.persistence.graph.MockJanusGraphProvider
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{ OneAppPerSuite, PlaySpec }
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{ JsObject, JsPath, JsValue }
import play.api.mvc.{ AnyContentAsEmpty, Result, Results }
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class SwaggerControllerSpec extends PlaySpec with OneAppPerSuite with MockitoSugar with Results {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides( bind[JWTVerifierProvider].to[MockJWTVerifierProvider] )
    .overrides( bind[JanusGraphProvider].to[MockJanusGraphProvider] )
    .build()

  val swaggerController: SwaggerController = app.injector.instanceOf[SwaggerController]

  "SwaggerController#getSwagger" should {
    "return a json object with host and schemes set" in {
      val result: Future[Result] = swaggerController.getSwagger().apply( FakeRequest().withBody( AnyContentAsEmpty ) )
      val bodyResult: JsValue = contentAsJson( result )

      val parseAsJsObject = bodyResult.validate[JsObject]
      parseAsJsObject mustBe 'success

      val obj = parseAsJsObject.get
      obj.keys must contain allOf ( "host", "schemes" )
    }

  }

}
