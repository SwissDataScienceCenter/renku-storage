package tasks.publish_events.kafka

import java.util.Properties

import javax.inject.{ Inject, Singleton }
import org.apache.kafka.clients.consumer.KafkaConsumer
import play.api.Configuration

@Singleton
class KafkaConsumerProvider @Inject() (
    protected val config: Configuration
) {

  def get(): KafkaConsumer[Long, Array[Byte]] = _kafkaConsumer

  protected lazy val _kafkaConsumer: KafkaConsumer[Long, Array[Byte]] = {
    val props = new Properties
    props.put( "bootstrap.servers", config.get[String]( "kafka.bootstrap.servers" ) )
    props.put( "group.id", config.get[String]( "kafka.group.id" ) )
    props.put( "enable.auto.commit", config.get[String]( "kafka.enable.auto.commit" ) )
    props.put( "key.deserializer", config.get[String]( "kafka.key.deserializer" ) )
    props.put( "value.deserializer", config.get[String]( "kafka.value.deserializer" ) )

    new KafkaConsumer[Long, Array[Byte]]( props )
  }

}
