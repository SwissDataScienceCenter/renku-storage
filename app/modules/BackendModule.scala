package modules

import controllers.storageBackends.{ Backend, LocalFSBackend, SwiftBackend }
import play.api.inject.{ Binding, Module }
import play.api.{ Configuration, Environment }

/**
 * Created by johann on 07/07/17.
 */
class BackendModule extends Module {

  def bindings( environment: Environment, configuration: Configuration ): Seq[Binding[Backend]] = {
    for {
      ( name, clazz ) <- availableBindings.toSeq
      if configuration.getBoolean( s"storage.backend.$name.enabled" ).getOrElse( false )
    } yield bind( classOf[Backend] ).qualifiedWith( name ).to( clazz )
  }

  protected def availableBindings: Map[String, Class[_ <: Backend]] = Map(
    "swift" -> classOf[SwiftBackend],
    "local" -> classOf[LocalFSBackend]
  )

}
