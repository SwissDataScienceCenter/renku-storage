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

package injected

import javax.inject.{Inject, Named}

import com.typesafe.config.{Config, ConfigFactory}
import groovy.lang.Singleton
import org.janusgraph.core.JanusGraphFactory
import org.janusgraph.diskstorage.configuration.ReadConfiguration
import play.api.Environment
import play.api.libs.concurrent.{ActorSystemProvider, Akka}

import scala.concurrent.ExecutionContext

/**
  * Created by johann on 12/04/17.
  */
class JanusGraphConfig @Inject()(protected val actorSystemProvider: ActorSystemProvider, protected val env: Environment) {

  protected lazy val config: Config = ConfigFactory.load().getConfig("janusgraph")

  def get: String = env.getFile(config.getString("file")).getPath

  def getExecutionContext: ExecutionContext = actorSystemProvider.get.dispatchers.lookup("janusgraph-execution-context")

}
