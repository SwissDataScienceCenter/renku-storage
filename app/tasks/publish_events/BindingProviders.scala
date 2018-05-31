package tasks.publish_events

import javax.inject.{ Inject, Named, Provider, Singleton }

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

object BindingProviders {

  class ActorSystemProvider extends Provider[ActorSystem] {
    def get(): ActorSystem = ActorSystem( "event-publisher" )
  }

  class ExecutionContextProvider @Inject() (
      @Named( "event-publisher" ) implicit val system:ActorSystem
  ) extends Provider[ExecutionContext] {
    def get(): ExecutionContext = {
      system.dispatchers.lookup( "event-publisher" )
    }
  }

}
