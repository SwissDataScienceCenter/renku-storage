package modules.eventPublisher.streams

import java.time.Instant

import akka.Done
import akka.stream.stage.{ GraphStage, GraphStageLogic, OutHandler, StageLogging }
import akka.stream.{ Attributes, Outlet, SourceShape }
import models.Event
import models.persistence.DatabaseLayer
import modules.EventExecutionContext
import play.api.db.slick.HasDatabaseConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.collection.mutable
import scala.concurrent.{ Future, blocking }

class PublisherSourceStage(
    val startFrom:                 Long,
    val fetchSize:                 Int,
    protected val dal:             DatabaseLayer,
    val dbConfig:                  DatabaseConfig[JdbcProfile],
    implicit val executionContext: EventExecutionContext
) extends GraphStage[SourceShape[Event]] with HasDatabaseConfig[JdbcProfile] {

  val out: Outlet[Event] = Outlet[Event]( "events.out" )

  def shape: SourceShape[Event] = SourceShape( out )

  def createLogic( inheritedAttributes: Attributes ): GraphStageLogic = {
    new GraphStageLogic( shape ) with StageLogging {
      private var lastEventId: Long = startFrom

      private val buffer: mutable.Queue[Event] = mutable.Queue[Event]()
      private def bufferIsFull: Boolean = buffer.lengthCompare( fetchSize ) > 0

      private var pollingDatabase: Boolean = false
      private var lastEmptyPoll: Option[Instant] = None

      setHandler( out, new OutHandler {
        def onPull(): Unit = {
          if ( buffer.nonEmpty ) {
            val event = buffer.dequeue()
            push( out, event )
          }

          if ( !bufferIsFull ) {
            pollDatabase()
          }
        }
      } )

      override def preStart(): Unit = {
        log.debug( "Stage SourceStage starting" )
        pollDatabase()
      }

      private def pollDatabase(): Unit = {
        if ( !pollingDatabase ) {
          pollingDatabase = true

          val callback = getAsyncCallback[Seq[Event]] { events =>
            pollingDatabase = false
            // enqueue events
            buffer.enqueue( events: _* )
            // if events, update cursor; else update lastEmptyPoll
            if ( events.nonEmpty ) {
              log.debug( s"Polled: $events" )
              lastEventId = events.last.id.get
              lastEmptyPoll = None
            }
            else {
              lastEmptyPoll = Some( Instant.now() )
            }
            // if needed, push event out
            if ( isAvailable( out ) && buffer.nonEmpty ) {
              val event = buffer.dequeue()
              push( out, event )
            }
            // poll again if buffer is not full
            if ( !bufferIsFull ) {
              pollDatabase()
            }
          }

          val wait = lastEmptyPoll match {
            case Some( i ) =>
              val duration = Instant.now().until( i.plusMillis( 100 ), java.time.temporal.ChronoUnit.MILLIS )
              if ( duration > 0 )
                Future { blocking { Thread.sleep( duration ); Done } }
              else
                Future.successful( Done )
            case None => Future.successful( Done )
          }

          wait.flatMap { _ =>
            db.run( dal.events.eventsFrom( lastEventId, fetchSize ) )
          }.foreach(
            callback.invoke _
          )
        }
      }

    }
  }
}
