package controllers.storageBackends

import javax.inject._

import scala.collection.JavaConverters._

@Singleton
class BackendModule @Inject()(config: play.api.Configuration){

  private lazy val swiftBackend = new SwiftBackend(config)

  private val availableBackends: Map[String, Unit => Backend] =
    Map(
      "swift" -> {_ => swiftBackend}
    )

  def get_backend(name: String): Option[Backend] =

    if (config.getStringList("storage.backends").map(_.asScala).getOrElse(List.empty).contains(name)) {
      Some(availableBackends(name)())
    } else {
      None
    }
}