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

import java.util.UUID
import javax.inject.{Inject, Singleton}

import authorization.JWTVerifierProvider
import ch.datascience.graph.Constants
import ch.datascience.graph.elements.mutation.create.CreateVertexOperation
import ch.datascience.graph.elements.mutation.{GraphMutationClient, Mutation}
import ch.datascience.graph.elements.new_.build.NewVertexBuilder
import ch.datascience.graph.elements.persisted.PersistedVertex
import ch.datascience.graph.elements.persisted.json.PersistedVertexFormat
import ch.datascience.graph.naming.NamespaceAndName
import ch.datascience.graph.values.{StringValue, UuidValue}
import ch.datascience.service.security.ProfileFilterAction
import ch.datascience.service.utils.{ControllerWithBodyParseJson, ControllerWithGraphTraversal}
import controllers.storageBackends.Backends
import ch.datascience.service.models.storage.json._
import ch.datascience.graph.elements.mutation.log.model.json._
import ch.datascience.service.models.storage.CreateBucketRequest
import ch.datascience.service.utils.persistence.graph.{GraphExecutionContextProvider, JanusGraphTraversalSourceProvider}
import ch.datascience.service.utils.persistence.reader.VertexReader

import scala.collection.JavaConversions._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import org.apache.tinkerpop.gremlin.structure.Vertex
import play.api.libs.ws.WSClient
import play.api.mvc.Controller

import scala.concurrent.Future

/**
  * Created by jeberle on 25.04.17.
  */
@Singleton
class ExplorerController @Inject()(config: play.api.Configuration,
                                   jwtVerifier: JWTVerifierProvider,
                                   wsclient: WSClient,
                                   backends: Backends,
                                   implicit val graphExecutionContextProvider: GraphExecutionContextProvider,
                                   implicit val janusGraphTraversalSourceProvider: JanusGraphTraversalSourceProvider,
                                   implicit val vertexReader: VertexReader
                                    ) extends Controller with ControllerWithBodyParseJson with ControllerWithGraphTraversal with RequestHelper{

  implicit lazy val persistedVertexFormat = PersistedVertexFormat


  def bucketBackends = ProfileFilterAction(jwtVerifier.get).async { implicit request =>
    Future(Ok(Json.toJson(backends.map.keys)))
  }


  def bucketList = ProfileFilterAction(jwtVerifier.get).async { implicit request =>
    val g = graphTraversalSource
    val t = g.V().has(Constants.TypeKey, "resource:bucket")

    val future: Future[Seq[PersistedVertex]] = graphExecutionContext.execute {
      Future.sequence(t.toIterable.map(v =>
        vertexReader.read(v)
      ).toSeq)
    }
    future.map(s => Ok(Json.toJson(s)))

  }

  def fileList(id: Long) =  ProfileFilterAction(jwtVerifier.get).async { implicit request =>
    val g = graphTraversalSource
    val t = g.V(Long.box(id)).in("resource:stored_in").has(Constants.TypeKey, "resource:file")

    val future: Future[Seq[PersistedVertex]] = graphExecutionContext.execute {
      Future.sequence(t.toIterable.map(v =>
        vertexReader.read(v)
      ).toSeq)
    }
    future.map(s => Ok(Json.toJson(s)))

  }

  def fileMetadatafromPath(id: Long, path: String) =  ProfileFilterAction(jwtVerifier.get).async { implicit request =>

    val g = graphTraversalSource
    val t = g.V().has("resource:filename",path).as("data").out("resource:stored_in").V(Long.box(id)).as("bucket").select[Vertex]("data", "bucket")

    Future.sequence(graphExecutionContext.execute {
      if (t.hasNext) {
        import scala.collection.JavaConverters._
        val jmap: Map[String, Vertex] = t.next().asScala.toMap
        for {
          (key, value) <- jmap
        } yield for {
          vertex <- vertexReader.read(value)
        } yield key -> vertex
      }
      else
        Seq.empty
    }).map(i => Ok(Json.toJson(i.toMap)))
  }

  def fileMetadata(id: Long) =  ProfileFilterAction(jwtVerifier.get).async { implicit request =>

    val g = graphTraversalSource
    val t = g.V(Long.box(id)).as("data").out("resource:stored_in").as("bucket").select[Vertex]("data", "bucket")

    Future.sequence(graphExecutionContext.execute {
      if (t.hasNext) {
        import scala.collection.JavaConverters._
        val jmap: Map[String, Vertex] = t.next().asScala.toMap
        for {
          (key, value) <- jmap
        } yield for {
          vertex <- vertexReader.read(value)
        } yield key -> vertex
      }
      else
        Seq.empty
    }).map(i => Ok(Json.toJson(i.toMap)))
  }

  def bucketMetadata(id: Long) =  ProfileFilterAction(jwtVerifier.get).async { implicit request =>

    getVertex(id).map {
      case Some(vertex) =>
        if (vertex.types.contains(NamespaceAndName("resource:bucket")))
          Ok(Json.toJson(vertex)(PersistedVertexFormat))
        else
          NotAcceptable // to differentiate from not found
      case None => NotFound
    }
  }

}
