package modules.eventPublisher.kafka

import java.util.Properties

import javax.inject.{ Inject, Singleton }
import org.apache.kafka.clients.admin.AdminClient
import play.api.Configuration

@Singleton
class KafkaAdminClientProvider @Inject() (
    protected val config: Configuration
) {

  def get(): AdminClient = _adminClient

  protected lazy val _adminClient: AdminClient = {
    val props = new Properties
    props.put( "bootstrap.servers", config.getString( "kafka.bootstrap.servers" ).get )

    AdminClient.create( props )
  }

  /*
  EventPublisherModule.addStopHook {
    _adminClient.close()
  }
  */

}
