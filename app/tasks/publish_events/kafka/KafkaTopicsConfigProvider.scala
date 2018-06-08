package tasks.publish_events.kafka

import javax.inject.{ Inject, Singleton }

import play.api.Configuration

@Singleton
class KafkaTopicsConfigProvider @Inject() (
    protected val config: Configuration
) {

  def get(): Seq[KafkaTopicConfig] = {
    val topicsConfig = config.getConfig( "kafka.topics" ).get
    for {
      topicName <- topicsConfig.subKeys.toSeq
    } yield {
      val c = topicsConfig.getConfig( topicName ).get
      val p = c.getInt( "partitions" ).getOrElse( 1 )
      val r = c.getInt( "replication" ).getOrElse( 1 )
      KafkaTopicConfig( topicName, p, r )
    }
  }

}
