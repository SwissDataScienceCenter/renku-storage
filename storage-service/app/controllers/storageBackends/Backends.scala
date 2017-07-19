package controllers.storageBackends

import javax.inject._

import play.api.Configuration
import play.api.inject.{BindingKey, Injector}

/**
  * Created by johann on 07/07/17.
  */
@Singleton
class Backends @Inject()(injector: Injector, configuration: Configuration) {

  val map: Map[String, Backend] = loadBackends

  def getBackend(name: String): Option[Backend] = map.get(name)

  private[this] def loadBackends: Map[String, Backend] = {
    val it = for {
      conf <- configuration.getConfig("storage.backend")
    } yield for {
      name <- conf.subKeys
      if conf.getBoolean(s"$name.enabled").getOrElse(false)
    } yield {
      val key = BindingKey(classOf[Backend]).qualifiedWith(name)
      name -> injector.instanceOf(key)
    }

    it.getOrElse(Seq.empty).toMap
  }

}
