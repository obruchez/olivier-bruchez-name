import play.api.Logger
import play.api.http._
import play.api.mvc.{Handler, RequestHeader}
import play.api.routing.Router
import play.core.DefaultWebCommands

import javax.inject.Inject

class RequestHandler @Inject() (
    router: Router,
    errorHandler: HttpErrorHandler,
    configuration: HttpConfiguration,
    filters: HttpFilters
) extends DefaultHttpRequestHandler(
      new DefaultWebCommands,
      None,
      () => router,
      errorHandler,
      configuration,
      filters.filters
    ) {

  override def routeRequest(request: RequestHeader): Option[Handler] = {
    Logger("access").info(s"Request from ${request.remoteAddress}: $request")
    super.routeRequest(request)
  }
}
