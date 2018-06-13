package tasks.publish_events.kafka

import javax.inject.{ Inject, Named, Singleton }
import models.Event
import org.apache.kafka.common.TopicPartition
import play.api.libs.json.{ Json, OFormat }
import play.api.{ Configuration, Logger }

import scala.concurrent.{ ExecutionContext, Future, blocking }

@Singleton
class RecoveryController @Inject() (
    protected val kafkaConsumerProvider:                     KafkaConsumerProvider,
    protected val config:                                    Configuration,
    @Named( "event-publisher" ) implicit val executionContext:ExecutionContext
) extends HasKafkaConsumerProvider {
  import scala.collection.JavaConverters._

  val topic: String = config.getOptional[String]( "events.push_to" ).getOrElse( "events" )

  lazy val logger: Logger = Logger( "application.modules.eventPublisher.RecoveryController" )

  implicit lazy val EventFormat: OFormat[Event] = Event.format

  val partitions: Seq[TopicPartition] = {
    val info = kafkaConsumer.partitionsFor( topic ).asScala.toSeq
    for ( obj <- info ) yield new TopicPartition( obj.topic(), obj.partition() )
  }

  logger.info( s"Found ${partitions.length} partitions for topic $topic" )

  def lastPushedEvent(): Future[Long] = {
    logger.debug( "Entered lastPushedEvent()" )
    // First, we seek to the end of each partition
    kafkaConsumer.assign( partitions.asJava )
    kafkaConsumer.seekToEnd( partitions.asJava )

    // Then for each partition where end > 0 (i.e. there is at least one message);
    // we get that last message
    val lastMessages: Future[Seq[Event]] = Future.sequence( partitions.map( getLastMessage ) ).map( _.flatten )

    for {
      seq <- lastMessages
    } yield {
      logger.debug( s"Last messages: $seq" )

      val ids = for {
        event <- seq
      } yield event.id.get
      val maxId = ( ids :+ -1L ).max //get max id or -1

      kafkaConsumer.unsubscribe()

      maxId
    }
  }

  def getLastMessage( partition: TopicPartition ): Future[Seq[Event]] = {

    val pos = kafkaConsumer.position( partition )
    logger.debug( s"Partition ${partition.topic()}#${partition.partition()}, position = $pos" )
    if ( pos > 0 ) {
      kafkaConsumer.seek( partition, pos - 1 )
      val future = Future {
        blocking {
          kafkaConsumer.poll( 1000 ).iterator().asScala.toSeq
        }
      }

      for {
        seq <- future
      } yield for {
        record <- seq
        event <- Json.parse( record.value() ).validate[Event].asOpt
      } yield event
    }
    else {
      Future.successful( Seq.empty )
    }
  }

}
