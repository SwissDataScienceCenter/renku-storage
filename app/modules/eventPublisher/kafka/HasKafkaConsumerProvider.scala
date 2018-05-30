package modules.eventPublisher.kafka

import org.apache.kafka.clients.consumer.KafkaConsumer

trait HasKafkaConsumerProvider {
  protected def kafkaConsumerProvider: KafkaConsumerProvider
  protected lazy val kafkaConsumer: KafkaConsumer[Long, Array[Byte]] = kafkaConsumerProvider.get()
}
