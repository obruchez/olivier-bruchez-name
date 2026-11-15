package controllers

import play.api.Logging
import play.api.mvc._
import util.Configuration

import java.awt.image.BufferedImage
import java.awt.{Color, Image}
import java.io.ByteArrayOutputStream
import java.util.concurrent.{
  ArrayBlockingQueue,
  ConcurrentHashMap,
  RejectedExecutionException,
  ThreadPoolExecutor,
  TimeUnit
}
import javax.imageio.stream.MemoryCacheImageOutputStream
import javax.imageio.{IIOImage, ImageIO, ImageWriteParam}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/** Serves small JPEG versions of the covers and posters stored (full size) with the data.
  *
  * Thumbnails are made on first request and kept in memory: a couple of thousand images of a few
  * kilobytes each.
  */
class CoversController @Inject() (val controllerComponents: ControllerComponents)(implicit
    executionContext: ExecutionContext
) extends BaseController
    with Logging {
  import CoversController._

  def cover(encodedPath: String) = Action.async {
    // The rest-of-path parameter comes as it is in the URL ("Le%20Horla.jpg")
    val path = java.net.URLDecoder.decode(encodedPath.replace("+", "%2B"), "UTF-8")

    if (!ValidPath.matches(path) || path.contains(".."))
      Future.successful(NotFound)
    else
      Option(thumbnails.get(path)) match {
        case Some(bytes) =>
          Future.successful(result(Some(bytes)))
        case None =>
          // A full queue rejects the task, either by throwing or by failing the future
          Try(Future(thumbnail(path))(using ioContext))
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

  private def thumbnail(path: String): Option[Array[Byte]] =
    Option(thumbnails.get(path)).orElse {
      Try(makeThumbnail(path)) match {
        case Success(bytes) =>
          if (thumbnails.size < MaxThumbnails) thumbnails.put(path, bytes)
          Some(bytes)
        case Failure(throwable) =>
          logger.warn(s"No thumbnail for $path: ${throwable.getMessage}")
          None
      }
    }

  private def makeThumbnail(path: String): Array[Byte] = {
    val url = Configuration.baseUrlWithFile(s"images/$path").get
    val original = Option(ImageIO.read(url)).getOrElse(sys.error("unsupported image format"))

    val width = math.min(ThumbnailWidth, original.getWidth)
    val height =
      math.max(1, math.round(original.getHeight.toDouble * width / original.getWidth).toInt)

    // White background: JPEG has no transparency
    val thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics = thumbnail.createGraphics()
    try {
      graphics.setColor(Color.WHITE)
      graphics.fillRect(0, 0, width, height)
      graphics.drawImage(original.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null)
    } finally graphics.dispose()

    val output = new ByteArrayOutputStream()
    val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
    val parameters = writer.getDefaultWriteParam
    parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
    parameters.setCompressionQuality(JpegQuality)

    val imageOutput = new MemoryCacheImageOutputStream(output)
    try {
      writer.setOutput(imageOutput)
      writer.write(null, new IIOImage(thumbnail, null, null), parameters)
    } finally {
      writer.dispose()
      imageOutput.close()
    }

    output.toByteArray
  }
}

object CoversController {
  // <list directory>/<file name>. No "?", "#" or "%" in the file name: they would be read as a query
  // or a fragment when the image is fetched, giving endless names (and cache entries) for one image
  private val ValidPath = """[a-z]+/[^/\\?#%]+\.(?i:jpe?g|png|gif)""".r

  // More than the number of images in the data, so that the cache cannot grow without bound
  private val MaxThumbnails = 10000
  // Only thumbnails that are not made yet wait in the queue
  private val MaxWaitingRequests = 2000

  // Displayed at about 70 CSS pixels wide: enough for high-density screens
  private val ThumbnailWidth = 220
  private val JpegQuality = 0.82f

  private val thumbnails = new ConcurrentHashMap[String, Array[Byte]]()

  // Fetching and resizing block: keep them away from the threads that serve pages
  private val ioContext: ExecutionContext =
    ExecutionContext.fromExecutor(
      new ThreadPoolExecutor(
        4,
        4,
        0L,
        TimeUnit.MILLISECONDS,
        new ArrayBlockingQueue[Runnable](MaxWaitingRequests),
        (runnable: Runnable) => {
          val thread = new Thread(runnable, "covers")
          thread.setDaemon(true)
          thread
        }
      )
    )
}
