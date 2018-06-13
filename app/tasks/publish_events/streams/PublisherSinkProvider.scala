package tasks.publish_events.streams

import akka.Done
import akka.stream.scaladsl.Sink
import akka.stream.{ Graph, SinkShape }
import javax.inject.{ Inject, Provider, Singleton }
import models.Event
import play.api.Configuration

import scala.concurrent.Future

@Singleton
class PublisherSinkProvider @Inject() (
    protected val config: Configuration
) extends Provider[Sink[Event, Future[Done]]] {

  def get(): Sink[Event, Future[Done]] = {
    val graph: Graph[SinkShape[Event], Future[Done]] = new PublisherSinkStage( config )
    Sink.fromGraph( graph )
  }

}
