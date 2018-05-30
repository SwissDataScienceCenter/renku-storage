package modules.eventPublisher.kafka

import org.apache.kafka.clients.admin.AdminClient

trait HasKafkaAdminClientProvider {
  protected def kafkaAdminClientProvider: KafkaAdminClientProvider
  protected lazy val kafkaAdminClient: AdminClient = kafkaAdminClientProvider.get()
}
