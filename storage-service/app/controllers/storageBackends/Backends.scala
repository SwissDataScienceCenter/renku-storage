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
      names <- configuration.getStringSeq("backends")
    } yield for {
      name <- names
    } yield {
      val key = BindingKey(classOf[Backend]).qualifiedWith(name)
      name -> injector.instanceOf(key)
    }

    it.getOrElse(Seq.empty).toMap
  }

}
