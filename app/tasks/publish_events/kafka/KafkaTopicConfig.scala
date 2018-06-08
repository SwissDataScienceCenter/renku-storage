package tasks.publish_events.kafka

case class KafkaTopicConfig(
    name:        String,
    partitions:  Int,
    replication: Int
)

