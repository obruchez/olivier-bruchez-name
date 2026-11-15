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

  def cover(encodedPath: String) = image(encodedPath, Covers)

  def blogImage(encodedPath: String) = image(encodedPath, BlogImages)

  private def image(encodedPath: String, kind: Kind) = Action.async {
    // The rest-of-path parameter comes as it is in the URL ("Le%20Horla.jpg")
    val path = java.net.URLDecoder.decode(encodedPath.replace("+", "%2B"), "UTF-8")
    val key = s"${kind.directory}/$path"

    // The patterns allow no "/" in the file name, so the path cannot leave its directory, and names
    // such as "Monsters, Inc..jpg" are fine
    if (!kind.validPath.matches(path))
      Future.successful(NotFound)
    else
      Option(thumbnails.get(key)) match {
        case Some(bytes) =>
          Future.successful(result(Some(bytes)))
        case None =>
          // A full queue rejects the task, either by throwing or by failing the future
          Try(Future(thumbnail(key, kind))(using ioContext))
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

  private def thumbnail(key: String, kind: Kind): Option[Array[Byte]] =
    Option(thumbnails.get(key)).orElse {
      Try(makeThumbnail(key, kind)) match {
        case Success(bytes) =>
          if (thumbnails.size < MaxThumbnails) thumbnails.put(key, bytes)
          Some(bytes)
        case Failure(throwable) =>
          logger.warn(s"No thumbnail for $key: ${throwable.getMessage}")
          None
      }
    }

  private def makeThumbnail(key: String, kind: Kind): Array[Byte] = {
    val url = Configuration.baseUrlWithFile(key).get
    val original = Option(ImageIO.read(url)).getOrElse(sys.error("unsupported image format"))

    val width = math.min(kind.width, original.getWidth)
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
    parameters.setCompressionQuality(kind.quality)

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

  /** A family of images: where they are under the data directory, and how they are served. */
  private case class Kind(
      directory: String,
      validPath: scala.util.matching.Regex,
      width: Int,
      quality: Float
  )

  // No "?", "#" or "%" in the file names: they would be read as a query or a fragment when the image
  // is fetched, giving endless names (and cache entries) for one image

  // <list directory>/<file name>; displayed at about 70 CSS pixels wide: enough for dense screens
  private val Covers =
    Kind("images", """[a-z]+/[^/\\?#%]+\.(?i:jpe?g|png|gif)""".r, width = 220, quality = 0.82f)

  // <year>/<month>/<file name>; at most as wide as the text column on a dense screen
  private val BlogImages =
    Kind(
      "blog/images",
      """\d{4}/\d{2}/[^/\\?#%]+\.(?i:jpe?g|png|gif)""".r,
      width = 1400,
      quality = 0.86f
    )

  // More than the number of images in the data, so that the cache cannot grow without bound
  private val MaxThumbnails = 10000
  // Only thumbnails that are not made yet wait in the queue
  private val MaxWaitingRequests = 2000

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
