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

import authorization.JWTVerifierProvider
import ch.datascience.graph.elements.persisted.PersistedVertex
import ch.datascience.graph.naming.NamespaceAndName
import ch.datascience.graph.values.StringValue
import ch.datascience.service.security.ProfileFilterAction
import ch.datascience.service.utils.ControllerWithBodyParseJson
import ch.datascience.service.models.resource.json._
import ch.datascience.service.models.storage.json._
import ch.datascience.service.models.storage.{CreateBucketRequest, ReadResourceRequest, WriteResourceRequest}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import clients.ResourcesManagerClient
import org.apache.tinkerpop.gremlin.structure.Vertex
import persistence.graph.{GraphExecutionContextProvider, JanusGraphTraversalSourceProvider}
import persistence.reader.VertexReader
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

/**
  * Created by jeberle on 25.04.17.
  */
@Singleton
class AuthorizeController @Inject()(config: play.api.Configuration,
                                    jwtVerifier: JWTVerifierProvider,
                                    implicit val wsclient: WSClient,
                                    implicit val graphExecutionContextProvider: GraphExecutionContextProvider,
                                    implicit val janusGraphTraversalSourceProvider: JanusGraphTraversalSourceProvider,
                                    implicit val vertexReader: VertexReader
                                    ) extends Controller with ControllerWithBodyParseJson with GraphTraversalComponent with RequestHelper {

  lazy val host: String = config
    .getString("resources.manager.service.host")
    .getOrElse("http://localhost:9000/api/resources/")

  def get_property(persistedVertex: PersistedVertex, name: String) =
    persistedVertex.properties.get(NamespaceAndName(name)).flatMap(v => v.values.headOption.map(value => value.asInstanceOf[StringValue].self))

  def objectRead = ProfileFilterAction(jwtVerifier.get).async(bodyParseJson[ReadResourceRequest](ReadResourceRequestFormat)) { implicit request =>

    implicit val token: String = request.headers.get("Authorization").getOrElse("")
    val rmc = new ResourcesManagerClient(host)
    val g = graphTraversalSource
    val t = g.V(Long.box(request.body.resourceId)).as("data").out("resource:stored_in").as("bucket").select[Vertex]("data", "bucket")

    graphExecutionContext.execute {
      if (t.hasNext) {
        import scala.collection.JavaConverters._
        val jmap: Map[String, Vertex] = t.next().asScala.toMap
        (for {
          data <- jmap.get("data").map(v => vertexReader.read(v))
          bucket <- jmap.get("bucket").map(v => vertexReader.read(v))
        } yield {
          for {d <- data; b <- bucket} yield {
            Some(Json.toJson(Map(
              "bucket" -> get_property(b, "resource:bucket_name").getOrElse(""),
              "name" -> get_property(d, "resource:file_name").getOrElse(""),
              "backend" -> get_property(b, "resource:bucket_backend").getOrElse("")
            )).as[JsObject])
          }
        }).getOrElse(Future(None))
      }
      else
        Future(None)
    }.flatMap(extra => {
      rmc.authorize(AccessRequestFormat, request.body.toAccessRequest(extra))
    }).map(ret => Ok(ret))

  }

  def objectWrite = ProfileFilterAction(jwtVerifier.get).async(bodyParseJson[WriteResourceRequest](WriteResourceRequestFormat)) { implicit request =>
    implicit val token: String = request.headers.get("Authorization").getOrElse("")
    val rmc = new ResourcesManagerClient(host)
    val g = graphTraversalSource
    val t = g.V(Long.box(request.body.resourceId)).as("data").out("resource:stored_in").as("bucket").select[Vertex]("data", "bucket")

    graphExecutionContext.execute {
      if (t.hasNext) {
        import scala.collection.JavaConverters._
        val jmap: Map[String, Vertex] = t.next().asScala.toMap
        (for {
          data <- jmap.get("data").map(v => vertexReader.read(v))
          bucket <- jmap.get("bucket").map(v => vertexReader.read(v))
        } yield {
          for {d <- data; b <- bucket} yield {
            Some(Json.toJson(Map(
              "bucket" -> get_property(b, "resource:bucket_name").getOrElse(""),
              "name" -> get_property(d, "resource:file_name").getOrElse(""),
              "backend" -> get_property(b, "resource:bucket_backend").getOrElse("")
            )).as[JsObject])
          }
        }).getOrElse(Future(None))
      }
      else
        Future(None)
    }.flatMap(extra => {
      rmc.authorize(AccessRequestFormat, request.body.toAccessRequest(extra))
    }).map(ret => Ok(ret))
  }

  def bucketCreate = ProfileFilterAction(jwtVerifier.get).async(bodyParseJson[CreateBucketRequest](CreateBucketRequestFormat)) { implicit request =>
    implicit val token: String = request.headers.get("Authorization").getOrElse("")
    val rmc = new ResourcesManagerClient(host)
    val extra = Some(Json.toJson(Map(
        "bucket" -> request.body.name,
        "backend" -> request.body.backend
      )).as[JsObject])
      rmc.authorize(AccessRequestFormat, request.body.toAccessRequest(extra)).map(res =>
          Ok(res)
      )
  }

}
