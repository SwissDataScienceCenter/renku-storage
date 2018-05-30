package modules.eventPublisher.kafka

case class KafkaTopicConfig(
    name:        String,
    partitions:  Int,
    replication: Int
)

