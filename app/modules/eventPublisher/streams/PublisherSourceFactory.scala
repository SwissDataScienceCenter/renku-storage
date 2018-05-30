package modules.eventPublisher.streams

import javax.inject.{ Inject, Singleton }
import akka.NotUsed
import akka.stream.{ Graph, SourceShape }
import akka.stream.scaladsl.Source
import models.Event
import models.persistence.DatabaseLayer
import modules.EventExecutionContext
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider

@Singleton
class PublisherSourceFactory @Inject() (
    protected val config:           Configuration,
    protected val dal:              DatabaseLayer,
    protected val dbConfigProvider: DatabaseConfigProvider,
    implicit val executionContext:  EventExecutionContext
) {

  val fetchSize: Int = config.getInt( "events.fetch_size" ).getOrElse( 1 )

  def make( startFrom: Long ): Source[Event, NotUsed] = {
    val graph: Graph[SourceShape[Event], NotUsed] = new PublisherSourceStage( startFrom, fetchSize, dal, dbConfigProvider.get, executionContext )
    Source.fromGraph( graph )
  }

}
