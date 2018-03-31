import javax.inject.Inject
import play.api.http._
import play.api.routing._
import play.api.mvc.{Handler, RequestHeader}
import play.api.Logger

class RequestHandler @Inject()(router: Router,
                               errorHandler: HttpErrorHandler,
                               configuration: HttpConfiguration,
                               filters: HttpFilters)
    extends DefaultHttpRequestHandler(router, errorHandler, configuration, filters) {

  override def routeRequest(request: RequestHeader): Option[Handler] = {
    Logger("access").info(s"Request from ${request.remoteAddress}: $request")
    super.routeRequest(request)
  }
}
