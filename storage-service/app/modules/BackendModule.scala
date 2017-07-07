package modules

import controllers.storageBackends.{Backend, SwiftBackend}
import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}

/**
  * Created by johann on 07/07/17.
  */
class BackendModule extends Module {

  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[Backend]] = {
    Seq(
      bind(classOf[Backend]).qualifiedWith("swift").to(classOf[SwiftBackend])
    )
  }

}
