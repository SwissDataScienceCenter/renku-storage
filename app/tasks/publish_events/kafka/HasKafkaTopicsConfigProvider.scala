package tasks.publish_events.kafka

trait HasKafkaTopicsConfigProvider {
  protected def kafkaTopicsConfigProvider: KafkaTopicsConfigProvider
  protected lazy val kafkaTopicsConfig: Seq[KafkaTopicConfig] = kafkaTopicsConfigProvider.get()
}
