package tasks.publish_events.kafka

import java.util.concurrent.ExecutionException
import javax.inject.{ Inject, Named, Singleton }

import akka.Done
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.errors.TopicExistsException
import play.api.Logger

import scala.concurrent.{ ExecutionContext, Future, blocking }
import scala.util.{ Success, Try }

@Singleton
class SetupKafkaTopics @Inject() (
    protected val kafkaTopicsConfigProvider:                 KafkaTopicsConfigProvider,
    protected val kafkaAdminClientProvider:                  KafkaAdminClientProvider,
    @Named( "event-publisher" ) implicit val executionContext:ExecutionContext
)
  extends HasKafkaTopicsConfigProvider
  with HasKafkaAdminClientProvider {

  def ensureTopics: Future[Done] = _future

  lazy val logger: Logger = Logger( "application.modules.eventPublisher.SetupKafkaTopics" )

  protected lazy val _future: Future[Done] = {
    import scala.collection.JavaConverters._

    val newTopics = for { topicConfig <- kafkaTopicsConfig } yield new NewTopic( topicConfig.name, topicConfig.partitions, topicConfig.replication.toShort )

    val result = kafkaAdminClient.createTopics( newTopics.asJava )

    val wait = Future {
      blocking {
        result.all().get()
      }
    }

    // Get result for each topic and ignore TopicExistsException
    val allResults = wait.map( _ =>
      result.values().asScala.toMap.mapValues { kf =>
        Try {
          kf.get()
        }.map { _ => Done }
          .recover {
            case e: ExecutionException => e.getCause match {
              case inner: TopicExistsException =>
                logger.info( inner.getMessage )
                Done
              case _ => throw e
            }
          }
      } )

    for {
      map <- allResults
      _ <- Future.sequence( for ( v <- map.values ) yield Future.fromTry( v ) )
    } yield Done
  }

}
