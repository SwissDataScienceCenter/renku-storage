package modules

import akka.actor.ActorSystem
import akka.stream.scaladsl.Keep
import akka.stream.{ ActorMaterializer, Materializer }
import javax.inject.{ Inject, Singleton }
import modules.eventPublisher.kafka.{ RecoveryController, SetupKafkaTopics }
import modules.eventPublisher.streams.{ PublisherSinkProvider, PublisherSourceFactory }
import play.api.{ Configuration, Environment }
import play.api.inject.Module

@Singleton
class EventPublisher @Inject() (
    val setupKafkaTopics:          SetupKafkaTopics,
    val recoveryController:        RecoveryController,
    val sourceFactory:             PublisherSourceFactory,
    val sinkProvider:              PublisherSinkProvider,
    implicit val executionContext: EventExecutionContext
) {
  implicit val system: ActorSystem = ActorSystem( "eventPublisher" )
  implicit val materializer: Materializer = ActorMaterializer()

  val futureGraph = for {
    _ <- setupKafkaTopics.ensureTopics
    lastEventId <- recoveryController.lastPushedEvent()
  } yield {
    val source = sourceFactory.make( lastEventId )
    source.toMat( sinkProvider.get() )( Keep.right )
  }

  val futureEnd = futureGraph.flatMap { graph =>
    graph.run()
  }
}

class EventPublisherModule extends Module {
  def bindings( environment: Environment, configuration: Configuration ) =
    Seq( bind[EventPublisher].toSelf.eagerly() )
}

