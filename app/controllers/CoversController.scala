package controllers

import play.api.mvc._
import util.Thumbnails

import java.util.concurrent.RejectedExecutionException
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/** Serves small JPEG versions of the covers and posters stored (full size) with the data: see
  * [[util.Thumbnails]].
  */
class CoversController @Inject() (val controllerComponents: ControllerComponents)(implicit
    executionContext: ExecutionContext
) extends BaseController {

  def cover(encodedPath: String) = image(encodedPath, Thumbnails.Covers)

  def blogImage(encodedPath: String) = image(encodedPath, Thumbnails.BlogImages)

  private def image(encodedPath: String, kind: Thumbnails.Kind) = Action.async {
    // The rest-of-path parameter comes as it is in the URL ("Le%20Horla.jpg")
    val path = java.net.URLDecoder.decode(encodedPath.replace("+", "%2B"), "UTF-8")
    val key = Thumbnails.key(kind, path)

    // The patterns allow no "/" in the file name, so the path cannot leave its directory, and names
    // such as "Monsters, Inc..jpg" are fine
    if (!kind.validPath.matches(path))
      Future.successful(NotFound)
    else
      Thumbnails.cached(key) match {
        case Some(bytes) =>
          Future.successful(result(Some(bytes)))
        case None =>
          // A full queue rejects the task, either by throwing or by failing the future
          Try(Future(Thumbnails.get(key, kind))(using Thumbnails.requestContext))
            .fold(Future.failed, identity)
            .map(result)
            .recover { case _: RejectedExecutionException => ServiceUnavailable }
      }
  }

  private def result(thumbnail: Option[Array[Byte]]): Result =
    thumbnail match {
      case Some(bytes) =>
        Ok(bytes).as("image/jpeg").withHeaders(CACHE_CONTROL -> "public, max-age=2592000")
      case None =>
        NotFound
    }
}
