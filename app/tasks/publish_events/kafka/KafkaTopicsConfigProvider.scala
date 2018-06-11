package tasks.publish_events.kafka

import javax.inject.{ Inject, Singleton }
import play.api.Configuration

@Singleton
class KafkaTopicsConfigProvider @Inject() (
    protected val config: Configuration
) {

  def get(): Seq[KafkaTopicConfig] = {
    val topicsConfig = config.get[Seq[Configuration]]( "kafka.topics" )
    for {
      c <- topicsConfig
    } yield {
      val n = c.get[String]( "name" )
      val p = c.getOptional[Int]( "partitions" ).getOrElse( 1 )
      val r = c.getOptional[Int]( "replication" ).getOrElse( 1 )
      KafkaTopicConfig( n, p, r )
    }
  }

}
