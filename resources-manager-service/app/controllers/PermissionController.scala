/*
 * Copyright 2017 - Swiss Data Science Center (SDSC)
 * A partnership between École Polytechnique Fédérale de Lausanne (EPFL) and
 * Eidgenössische Technische Hochschule Zürich (ETHZ).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import javax.inject.{Inject, Singleton}

import authorization.{JWTVerifierProvider, TokenSignerProvider}
import ch.datascience.service.models.resource.json._
import ch.datascience.service.models.resource.{AccessRequest, ScopeQualifier}
import ch.datascience.service.security.TokenFilterAction
import ch.datascience.service.utils.ControllerWithBodyParseJson
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.{JWT, JWTVerifier}
import persistence.graph.{GraphExecutionContextProvider, JanusGraphTraversalSourceProvider}
import persistence.reader.VertexReader
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

/**
  * Created by jeberle on 25.04.17.
  */
@Singleton
class PermissionController @Inject() (
  verifierProvider: JWTVerifierProvider,
  tokenSignerProvider: TokenSignerProvider,
  implicit val graphExecutionContextProvider: GraphExecutionContextProvider,
  implicit val janusGraphTraversalSourceProvider: JanusGraphTraversalSourceProvider,
  implicit val vertexReader: VertexReader
) extends Controller with ControllerWithBodyParseJson with GraphTraversalComponent {

  val verifier: JWTVerifier = verifierProvider.get

  def authorize: Action[AccessRequest] = TokenFilterAction(verifier).async(bodyParseJson[AccessRequest]) { implicit request =>
    val accessRequest = request.body
    val accessToken = request.token

    val futureScopes = accessRequest.permissionHolderId match {
      case Some(resourceId) =>
        authorizeAccess(accessToken, resourceId, accessRequest.scope)
      case None =>
        authorizeGlobalAccess(accessToken, accessRequest.scope)
    }

    val futureToken = for {
      scopes <- futureScopes
    } yield {
      val tokenBuilder = JWT.create()
      tokenBuilder.withSubject(accessToken.getSubject)
      for (resourceId <- accessRequest.permissionHolderId) {
        tokenBuilder.withClaim("resource_id", Long.box(resourceId))
      }
      tokenBuilder.withArrayClaim("resource_scope", scopes.toArray.map(_.toString))
      tokenSignerProvider.addDefaultHeadersAndClaims(tokenBuilder)
      tokenBuilder.sign(tokenSignerProvider.get)
    }

    for {
      token <- futureToken
    } yield {
      Ok(Json.toJson(JsObject(Map("access_token" -> JsString(token)))))
    }
  }

  def authorizeAccess(accessToken: DecodedJWT, resourceId: Long, scopes: Set[ScopeQualifier]): Future[Set[ScopeQualifier]] = {
    val g = graphTraversalSource
    val t = g.V(Long.box(resourceId))

    val futureVertex = Future {
      graphExecutionContext.execute {
        if (t.hasNext)
          Some(t.next())
        else
          None
      }
    }

    val futurePersistedVertex = futureVertex.flatMap {
      case Some(v) => vertexReader.read(v).map(Some.apply)
      case None => Future.successful(None)
    }

    val futureOptScopes = for {
      optVertex <- futurePersistedVertex
    } yield for {
      vertex <- optVertex
    } yield {
      // TODO: perform ABAC here, using vertex, accessToken and scopes
      scopes
    }

    futureOptScopes.map(_.getOrElse(Set.empty))
  }

  def authorizeGlobalAccess(accessToken: DecodedJWT, scopes: Set[ScopeQualifier]): Future[Set[ScopeQualifier]] = {
    // TODO: perform ABAC, using accessToken and scopes
    Future.successful(scopes)
  }

}
