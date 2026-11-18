import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.routing.Router
import play.api.{Configuration, Environment, OptionalSourceMapper}

import javax.inject.{Inject, Provider, Singleton}
import scala.concurrent.Future

/** Loaded automatically by Play (root package `ErrorHandler`): an address that matches no route
  * gets the same "not found" page as a blog post or book notes that do not exist.
  */
@Singleton
class ErrorHandler @Inject() (
    environment: Environment,
    configuration: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router]
) extends DefaultHttpErrorHandler(environment, configuration, sourceMapper, router) {
  override protected def onNotFound(request: RequestHeader, message: String): Future[Result] =
    Future.successful(Results.NotFound(views.html.notFound(request.path)))
}
