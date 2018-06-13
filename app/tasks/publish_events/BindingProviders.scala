package tasks.publish_events

import akka.actor.ActorSystem
import javax.inject.{ Inject, Named, Provider }

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
