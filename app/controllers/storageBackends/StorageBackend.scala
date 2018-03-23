package controllers.storageBackends

import ch.datascience.service.security.RequestWithProfile
import models.Repository

import scala.concurrent.Future

trait StorageBackend {
  def createRepo( request: Repository ): Future[Option[String]]
}
