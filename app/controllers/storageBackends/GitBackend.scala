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

package controllers.storageBackends

import akka.util.ByteString
import ch.datascience.service.security.RequestWithProfile
import models.Repository
import play.api.libs.streams.Accumulator
import play.api.mvc.{ RequestHeader, Result }

import scala.concurrent.Future

/**
 * Created by jeberle on 07.07.17.
 */
trait GitBackend extends StorageBackend {

  def getRefs( request: RequestHeader, url: String, user: String ): Future[Result]
  def upload( req: RequestHeader, url: String, user: String ): Accumulator[ByteString, Result]
  def receive( req: RequestHeader, url: String, user: String ): Accumulator[ByteString, Result]
}
