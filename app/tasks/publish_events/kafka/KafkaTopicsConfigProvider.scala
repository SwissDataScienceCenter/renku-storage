package tasks.publish_events.kafka

import javax.inject.{ Inject, Singleton }

import play.api.Configuration

@Singleton
class KafkaTopicsConfigProvider @Inject() (
    protected val config: Configuration
) {

  def get(): Seq[KafkaTopicConfig] = {
    val topicsConfig = config.get[Configuration]( "kafka.topics" )
    for {
      topicName <- topicsConfig.subKeys.toSeq
    } yield {
      val c = topicsConfig.get[Configuration]( topicName )
      val p = c.getOptional[Int]( "partitions" ).getOrElse( 1 )
      val r = c.getOptional[Int]( "replication" ).getOrElse( 1 )
      KafkaTopicConfig( topicName, p, r )
    }
  }

}
