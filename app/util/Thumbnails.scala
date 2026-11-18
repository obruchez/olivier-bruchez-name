package util

import play.api.Logging

import java.awt.image.BufferedImage
import java.awt.{Color, Image}
import java.io.{ByteArrayOutputStream, InputStream}
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import java.security.MessageDigest
import java.util.concurrent.{
  ArrayBlockingQueue,
  BlockingQueue,
  ConcurrentHashMap,
  LinkedBlockingQueue,
  ThreadPoolExecutor,
  TimeUnit
}
import javax.imageio.stream.MemoryCacheImageOutputStream
import javax.imageio.{IIOImage, ImageIO, ImageWriteParam}
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

/** Small JPEG versions of the covers, posters and blog images stored (full size) with the data.
  *
  * A thumbnail is made the first time it is needed, then kept in memory and, when a directory is
  * configured (thumbnails.directory), on disk: a restart then finds it again instead of fetching
  * and resizing the original once more. Each thumbnail found on disk is checked against its
  * original once per run, in the background, so that a replaced image ends up replaced here too.
  */
object Thumbnails extends Logging {

  /** A family of images: where they are under the data directory, and how they are served. */
  case class Kind(
      directory: String,
      validPath: scala.util.matching.Regex,
      width: Int,
      quality: Float
  )

  // No "?", "#" or "%" in the file names: they would be read as a query or a fragment when the image
  // is fetched, giving endless names (and cache entries) for one image

  // <list directory>/<file name>; displayed at about 70 CSS pixels wide: enough for dense screens
  val Covers: Kind =
    Kind("images", """[a-z]+/[^/\\?#%]+\.(?i:jpe?g|png|gif)""".r, width = 220, quality = 0.82f)

  // <year>/<month>/<file name>; at most as wide as the text column on a dense screen
  val BlogImages: Kind =
    Kind(
      "blog/images",
      """\d{4}/\d{2}/[^/\\?#%]+\.(?i:jpe?g|png|gif)""".r,
      width = 1400,
      quality = 0.86f
    )

  // More than the number of images in the data, so that the cache cannot grow without bound
  private val MaxThumbnails = 10000
  // Only thumbnails that are not in memory yet wait in the queue
  private val MaxWaitingRequests = 2000

  private val ConnectTimeoutMs = 10000
  private val ReadTimeoutMs = 30000

  private val memory = new ConcurrentHashMap[String, Array[Byte]]()
  // Thumbnails known to match their original, as far as this run is concerned
  private val checked = ConcurrentHashMap.newKeySet[String]()

  private def executionContext(name: String, queue: BlockingQueue[Runnable]): ExecutionContext =
    ExecutionContext.fromExecutor(
      new ThreadPoolExecutor(
        4,
        4,
        0L,
        TimeUnit.MILLISECONDS,
        queue,
        (runnable: Runnable) => {
          val thread = new Thread(runnable, name)
          thread.setDaemon(true)
          thread
        }
      )
    )

  /** Fetching and resizing block: keep them away from the threads that serve pages. A full queue
    * rejects the task.
    */
  val requestContext: ExecutionContext =
    executionContext("thumbnails", new ArrayBlockingQueue[Runnable](MaxWaitingRequests))

  // Warm-up and checks have their own threads, so that a visitor never waits behind them
  private val backgroundContext: ExecutionContext =
    executionContext("thumbnails-background", new LinkedBlockingQueue[Runnable]())

  private lazy val directory: Option[Path] =
    Configuration.string("thumbnails.directory").map(_.trim).filter(_.nonEmpty).flatMap { name =>
      Try(Files.createDirectories(Paths.get(name))) match {
        case Success(path) if Files.isWritable(path) =>
          logger.info(s"Thumbnails kept in $path")
          Some(path)
        case Success(path) =>
          logger.warn(s"Thumbnails kept in memory only: $path is not writable")
          None
        case Failure(throwable) =>
          logger.warn(s"Thumbnails kept in memory only: ${throwable.getMessage}")
          None
      }
    }

  def key(kind: Kind, path: String): String = s"${kind.directory}/$path"

  /** The thumbnail if it is in memory: does not block. */
  def cached(key: String): Option[Array[Byte]] = Option(memory.get(key))

  /** The thumbnail, read from the disk or made from its original if need be: blocks. */
  def get(key: String, kind: Kind): Option[Array[Byte]] =
    cached(key).orElse {
      fromDisk(key, kind) match {
        case Some(bytes) =>
          remember(key, bytes)
          if (checked.add(key)) backgroundContext.execute(() => { update(key, kind); () })
          Some(bytes)
        case None =>
          update(key, kind)
      }
    }

  /** Gets these thumbnails ready in the background, and checks those that were already on disk. */
  def warmUp(keys: Seq[String], kind: Kind): Unit =
    keys.distinct.filterNot(checked.contains).foreach { key =>
      backgroundContext.execute { () =>
        if (checked.add(key)) {
          fromDisk(key, kind).foreach(remember(key, _))
          update(key, kind)
          ()
        }
      }
    }

  private def remember(key: String, bytes: Array[Byte]): Unit =
    if (memory.size < MaxThumbnails) memory.put(key, bytes)

  /** Makes the thumbnail again if there is none or if its original has changed.
    *
    * @return
    *   the thumbnail, new or not; the previous one if the original cannot be read
    */
  private def update(key: String, kind: Kind): Option[Array[Byte]] = {
    val current = cached(key).orElse(fromDisk(key, kind))
    val currentVersion = current.flatMap(_ => versionOnDisk(key, kind))

    Try(fetch(key, currentVersion).map { case (original, version) =>
      (resized(original, kind), version)
    }) match {
      case Success(Some((bytes, version))) =>
        toDisk(key, kind, bytes, version)
        memory.remove(key)
        remember(key, bytes)
        checked.add(key)
        Some(bytes)
      case Success(None) =>
        checked.add(key)
        current
      case Failure(throwable) =>
        logger.warn(s"No thumbnail for $key: ${throwable.getMessage}")
        current
    }
  }

  /** Reads the original, unless it is still at the given version.
    *
    * @return
    *   the image and its version (what tells one state of the original from the next), or None if
    *   the original has not changed
    */
  private def fetch(
      key: String,
      currentVersion: Option[String]
  ): Option[(BufferedImage, Option[String])] = {
    val connection = Configuration.baseUrlWithFile(key).get.openConnection()
    connection.setConnectTimeout(ConnectTimeoutMs)
    connection.setReadTimeout(ReadTimeoutMs)

    def image(input: InputStream): BufferedImage =
      try Option(ImageIO.read(input)).getOrElse(sys.error("unsupported image format"))
      finally input.close()

    connection match {
      case http: HttpURLConnection =>
        try {
          currentVersion.foreach(http.setRequestProperty("If-None-Match", _))

          http.getResponseCode match {
            case HttpURLConnection.HTTP_NOT_MODIFIED => None
            case HttpURLConnection.HTTP_OK           =>
              Some((image(http.getInputStream), Option(http.getHeaderField("ETag"))))
            case code => sys.error(s"HTTP $code")
          }
        } finally http.disconnect()
      case other =>
        // A local file: the stream has to be opened for the headers to be known
        val input = other.getInputStream
        val version = s"${other.getLastModified}-${other.getContentLengthLong}"

        if (currentVersion.contains(version)) {
          input.close()
          None
        } else
          Some((image(input), Some(version)))
    }
  }

  private def resized(original: BufferedImage, kind: Kind): Array[Byte] = {
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

  // On disk: <name>.jpg for the thumbnail and <name>.version for the version of its original. The
  // name depends on the size and quality too, so that changing them here makes new thumbnails.
  private def fileName(key: String, kind: Kind): String =
    MessageDigest
      .getInstance("SHA-1")
      .digest(s"$key|${kind.width}|${kind.quality}".getBytes(UTF_8))
      .map("%02x".format(_))
      .mkString

  private def thumbnailFile(path: Path, key: String, kind: Kind): Path =
    path.resolve(fileName(key, kind) + ".jpg")

  private def versionFile(path: Path, key: String, kind: Kind): Path =
    path.resolve(fileName(key, kind) + ".version")

  private def fromDisk(key: String, kind: Kind): Option[Array[Byte]] =
    directory.flatMap { path =>
      Try(Files.readAllBytes(thumbnailFile(path, key, kind))).toOption.filter(_.nonEmpty)
    }

  private def versionOnDisk(key: String, kind: Kind): Option[String] =
    directory.flatMap { path =>
      Try(new String(Files.readAllBytes(versionFile(path, key, kind)), UTF_8)).toOption
        .filter(_.nonEmpty)
    }

  private def toDisk(key: String, kind: Kind, bytes: Array[Byte], version: Option[String]): Unit =
    directory.foreach { path =>
      // Written under another name, then renamed: a thumbnail on disk is always a whole one
      def write(file: Path, content: Array[Byte]): Unit = {
        val temporary = Files.createTempFile(path, "thumbnail", ".tmp")
        try {
          Files.write(temporary, content)
          Files.move(
            temporary,
            file,
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE
          )
        } finally Files.deleteIfExists(temporary)
      }

      Try {
        // Without its version, a thumbnail is simply made again at the next check
        Files.deleteIfExists(versionFile(path, key, kind))
        write(thumbnailFile(path, key, kind), bytes)
        version.foreach(version => write(versionFile(path, key, kind), version.getBytes(UTF_8)))
      } match {
        case Success(_)         =>
        case Failure(throwable) =>
          logger.warn(s"Thumbnail for $key not written to disk: ${throwable.getMessage}")
      }
    }
}
