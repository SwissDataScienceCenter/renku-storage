package controllers.storageBackends

import models.Repository

import scala.concurrent.Future

trait StorageBackend {
  def createRepo( request: Repository ): Future[Option[String]]
}
