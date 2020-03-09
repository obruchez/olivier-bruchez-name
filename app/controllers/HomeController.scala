package controllers

import javax.inject._
import play.api._
import play.api.mvc._

@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def test1() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.test1())
  }

  def test2() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.test2())
  }

  def test3() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.test3())
  }
}
