package tasks.publish_events.streams

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.stream.{ Graph, SourceShape }
import javax.inject.{ Inject, Named, Singleton }
import models.Event
import models.persistence.DatabaseLayer
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

@Singleton
class PublisherSourceFactory @Inject() (
    protected val config:                                    Configuration,
    protected val dal:                                       DatabaseLayer,
    protected val dbConfigProvider:                          DatabaseConfigProvider,
    @Named( "event-publisher" ) implicit val executionContext:ExecutionContext
) {

  val fetchSize: Int = config.getOptional[Int]( "events.fetch_size" ).getOrElse( 1 )

  def make( startFrom: Long ): Source[Event, NotUsed] = {
    val graph: Graph[SourceShape[Event], NotUsed] = new PublisherSourceStage( startFrom, fetchSize, dal, dbConfigProvider.get, executionContext )
    Source.fromGraph( graph )
  }

}
