package modules

import akka.actor.ActorSystem
import akka.stream.scaladsl.Keep
import akka.stream.{ ActorMaterializer, Materializer }
import javax.inject.{ Inject, Named, Singleton }

import tasks.publish_events.kafka.{ RecoveryController, SetupKafkaTopics }
import tasks.publish_events.streams.{ PublisherSinkProvider, PublisherSourceFactory }
import play.api.{ Configuration, Environment }
import play.api.inject.{ Binding, Module }
import tasks.publish_events.EventPublisher

import tasks.publish_events.BindingProviders

import scala.concurrent.ExecutionContext

class EventPublisherModule extends Module {
  def bindings( environment: Environment, configuration: Configuration ): Seq[Binding[_]] =
    Seq(
      bind[ActorSystem].qualifiedWith( "event-publisher" ).toProvider( classOf[BindingProviders.ActorSystemProvider] ),
      bind[ExecutionContext].qualifiedWith( "event-publisher" ).toProvider( classOf[BindingProviders.ExecutionContextProvider] ),
      bind[EventPublisher].toSelf.eagerly()
    )
}
