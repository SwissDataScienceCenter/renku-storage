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
    props.put( "bootstrap.servers", config.getString( "kafka.bootstrap.servers" ).get )
    props.put( "group.id", config.getString( "kafka.group.id" ).get )
    props.put( "enable.auto.commit", config.getString( "kafka.enable.auto.commit" ).get )
    props.put( "key.deserializer", config.getString( "kafka.key.deserializer" ).get )
    props.put( "value.deserializer", config.getString( "kafka.value.deserializer" ).get )

    new KafkaConsumer[Long, Array[Byte]]( props )
  }

  /*
  EventPublisherModule.addStopHook {
    _kafkaConsumer.unsubscribe()
    _kafkaConsumer.close()
  }
  */
}
