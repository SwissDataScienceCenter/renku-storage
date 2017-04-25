package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, Controller}

/**
  * Created by jeberle on 25.04.17.
  */
@Singleton
class AccessController @Inject() extends Controller {

  def token = Action { implicit request =>

    Ok()
  }

}
