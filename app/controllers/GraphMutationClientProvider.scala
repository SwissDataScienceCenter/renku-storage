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
