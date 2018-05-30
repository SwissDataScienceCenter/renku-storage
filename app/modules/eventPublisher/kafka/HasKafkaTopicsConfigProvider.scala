package modules.eventPublisher.kafka

trait HasKafkaTopicsConfigProvider {
  protected def kafkaTopicsConfigProvider: KafkaTopicsConfigProvider
  protected lazy val kafkaTopicsConfig: Seq[KafkaTopicConfig] = kafkaTopicsConfigProvider.get()
}
