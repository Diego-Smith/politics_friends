package controllers

import play.api.mvc.{Action, Controller}

object Application extends Controller {
  def index = Action {
    val x:Option[Long] = Some(1L)
    Ok(views.html.index("Hello Play Framework"))
  }
}