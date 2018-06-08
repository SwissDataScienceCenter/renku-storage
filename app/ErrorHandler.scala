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

import javax.inject.{ Inject, Provider, Singleton }
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.Results.Redirect
import play.api.mvc.{ RequestHeader, Result }
import play.api.routing.Router
import play.api.{ Configuration, Environment, OptionalSourceMapper }

import scala.concurrent.Future
import scala.util.matching.Regex

@Singleton
class ErrorHandler @Inject() (
    env:          Environment,
    config:       Configuration,
    sourceMapper: OptionalSourceMapper,
    router:       Provider[Router]
) extends DefaultHttpErrorHandler( env, config, sourceMapper, router ) {

  val endsWithASlash: Regex = "/(.*)/$".r

  override protected def onNotFound( request: RequestHeader, message: String ): Future[Result] = request.path match {
    case endsWithASlash( _ ) =>
      val newRequest = request.withTarget( request.target.withPath( request.path.dropRight( 1 ) ) )
      router.get().handlerFor( newRequest ) match {
        case Some( _ ) => Future.successful( Redirect( newRequest.path, request.queryString, 301 ) )
        case None      => super.onNotFound( request, message )
      }
    case _ => super.onNotFound( request, message )
  }

}
