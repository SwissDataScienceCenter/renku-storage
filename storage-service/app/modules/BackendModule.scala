package modules

import controllers.storageBackends.{Backend, LocalFSBackend, SwiftBackend}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

/**
  * Created by johann on 07/07/17.
  */
class BackendModule extends Module {

  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[Backend]] = {
    Seq(
      bind(classOf[Backend]).qualifiedWith("swift").to(classOf[SwiftBackend]),
      bind(classOf[Backend]).qualifiedWith("local").to(classOf[LocalFSBackend])
    )
  }

}
