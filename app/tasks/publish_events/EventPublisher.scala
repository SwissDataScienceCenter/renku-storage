package tasks.publish_events

import akka.actor.ActorSystem
import akka.stream.scaladsl.Keep
import akka.stream.{ ActorMaterializer, Materializer }
import javax.inject.{ Inject, Named, Singleton }
import play.api.Logger
import tasks.publish_events.kafka.{ RecoveryController, SetupKafkaTopics }
import tasks.publish_events.streams.{ PublisherSinkProvider, PublisherSourceFactory }

import scala.concurrent.ExecutionContext

@Singleton
class EventPublisher @Inject() (
    val setupKafkaTopics:                                    SetupKafkaTopics,
    val recoveryController:                                  RecoveryController,
    val sourceFactory:                                       PublisherSourceFactory,
    val sinkProvider:                                        PublisherSinkProvider,
    @Named( "event-publisher" ) implicit val system:         ActorSystem,
    @Named( "event-publisher" ) implicit val executionContext:ExecutionContext
) {

  lazy val logger: Logger = Logger( "application.EventPublisher" )

  def start(): Unit = {
    implicit val materializer: Materializer = ActorMaterializer()

    val futureGraph = for {
      _ <- setupKafkaTopics.ensureTopics
      lastEventId <- recoveryController.lastPushedEvent()
    } yield {
      val source = sourceFactory.make( lastEventId )
      source.toMat( sinkProvider.get() )( Keep.right )
    }

    for {
      graph <- futureGraph
    } yield {
      logger.info( "Starting event publisher" )
      graph.run()
    }
  }

  start()
}
