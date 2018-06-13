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

package modules

import controllers.storageBackends._
import play.api.inject.{ Binding, Module }
import play.api.{ Configuration, Environment }

/**
 * Created by johann on 07/07/17.
 */
class BackendModule extends Module {

  def bindings( environment: Environment, configuration: Configuration ): Seq[Binding[StorageBackend]] = {
    for {
      ( name, clazz ) <- availableBindings.toSeq
      if configuration.getOptional[Boolean]( s"storage.backend.$name.enabled" ).getOrElse( false )
    } yield bind( classOf[StorageBackend] ).qualifiedWith( name ).to( clazz )
  }

  protected def availableBindings: Map[String, Class[_ <: StorageBackend]] = Map(
    "swift" -> classOf[SwiftObjectBackend],
    "local" -> classOf[LocalFSObjectBackend],
    "s3" -> classOf[S3ObjectBackend],
    "azure" -> classOf[AzureObjectBackend],
    "gitlab" -> classOf[GitlabBackend],
    "localgit" -> classOf[LocalGitBackend]
  )

}
