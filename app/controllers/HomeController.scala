package controllers

import play.api.mvc._

import javax.inject._

@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def test1(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.test1())
  }

  def test2(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.test2())
  }

  def test3(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.test3())
  }
}
