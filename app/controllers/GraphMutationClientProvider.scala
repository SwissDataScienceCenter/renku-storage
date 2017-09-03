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

import javax.inject.{ Inject, Singleton }

import ch.datascience.graph.elements.mutation.GraphMutationClient
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

@Singleton
class GraphMutationClientProvider @Inject() (
    config:        Configuration,
    tokenProvider: TokenProvider,
    ws:            WSClient,
    ec:            ExecutionContext
) {

  lazy val mhost: String = config
    .getString( "graph.mutation.service.host" )
    .getOrElse( "http://graph-mutation:9000/api/mutation" )

  def get: GraphMutationClient = GraphMutationClient( mhost, tokenProvider, ec, ws )

}
