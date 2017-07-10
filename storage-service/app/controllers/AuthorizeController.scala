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

import ch.datascience.graph.Constants
import ch.datascience.graph.elements.mutation.create.CreateVertexOperation
import ch.datascience.graph.elements.mutation.{GraphMutationClient, Mutation}
import ch.datascience.graph.elements.new_.build.NewVertexBuilder
import ch.datascience.graph.elements.persisted.PersistedVertex
import ch.datascience.graph.elements.persisted.json.PersistedVertexFormat
import ch.datascience.graph.naming.NamespaceAndName
import ch.datascience.graph.values.{StringValue, UuidValue}
import models.{CreateBucketRequest, ReadResourceRequest, WriteResourceRequest}
import models.json._
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.pac4j.play.store.PlaySessionStore
import persistence.graph.{GraphExecutionContextProvider, JanusGraphTraversalSourceProvider}
import persistence.reader.VertexReader
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import clients.ResourcesManagerClient

import scala.collection.JavaConversions._
import scala.concurrent.Future

/**
  * Created by jeberle on 25.04.17.
  */
@Singleton
class AuthorizeController @Inject()(config: play.api.Configuration,
                                    implicit val playSessionStore: PlaySessionStore,
                                    implicit val wsclient: WSClient
                                    ) extends Controller with JsonComponent with RequestHelper{

  lazy val host: String = config
    .getString("resources.manager.service.host")
    .getOrElse("http://localhost:9000/api/resources/")

  def objectRead = Action.async(bodyParseJson[ReadResourceRequest](readResourceRequestReads)) { implicit request =>
    Future {
      val rrr = request.body
      val rmc = new ResourcesManagerClient
      rmc.authorize(readResourceRequestWrites, rrr)

      // TODO Check permission and forward token

      Ok
    }
  }

  def objectWrite = Action.async(bodyParseJson[WriteResourceRequest](writeResourceRequestReads)) { implicit request =>
    Future {
      val rrr = request.body
      val rmc = new ResourcesManagerClient
      rmc.authorize(writeResourceRequestWrites, rrr)

      // TODO Check permission and forward token

      Ok
    }
  }

  def bucketCreate = Action.async(bodyParseJson[CreateBucketRequest](createBucketRequestReads)) { implicit request =>
    Future{
  val rrr = request.body
  val rmc = new ResourcesManagerClient
  rmc.authorize(createBucketRequestWrites, rrr)

  // TODO Check permission and forward token

  Ok
    }

  }
}
