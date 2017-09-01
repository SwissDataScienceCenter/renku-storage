package controllers

import javax.inject.{ Inject, Singleton }

import ch.datascience.service.security.{ TokenProvider => Base }
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

@Singleton
class TokenProvider @Inject() (
    config: Configuration,
    ws:     WSClient,
    ec:     ExecutionContext
) extends Base( config.getConfig( "authorization" ).get, ws )( ec )
