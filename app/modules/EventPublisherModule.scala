package modules

import akka.actor.ActorSystem
import play.api.inject.{ Binding, Module }
import play.api.{ Configuration, Environment }
import tasks.publish_events.{ BindingProviders, EventPublisher }

import scala.concurrent.ExecutionContext

class EventPublisherModule extends Module {
  def bindings( environment: Environment, configuration: Configuration ): Seq[Binding[_]] =
    Seq(
      bind[ActorSystem].qualifiedWith( "event-publisher" ).toProvider( classOf[BindingProviders.ActorSystemProvider] ),
      bind[ExecutionContext].qualifiedWith( "event-publisher" ).toProvider( classOf[BindingProviders.ExecutionContextProvider] ),
      bind[EventPublisher].toSelf.eagerly()
    )
}
