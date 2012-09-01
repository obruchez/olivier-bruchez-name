package bootstrap.liftweb

import net.liftweb.common.Box
import net.liftweb.http._

object OldSiteDispatch extends LiftRules.DispatchPF {
  override def isDefinedAt(req: Req): Boolean = LiftRules.getResource(resourceNameFromReq(req)).isDefined

  override def apply(req: Req): () => Box[LiftResponse] = () => {
    val resourceName = resourceNameFromReq(req)
    for {
      extension <- Option(req.path.suffix).map(_.trim).filter(_.nonEmpty) orElse // Use suffix from Req...
        req.path.partPath.lastOption.flatMap(_.split("""\.""").lastOption) // ...or compute suffix if dots in name
      mimeType <- mimeTypes.get(extension)
      resourceLength <- resourceLength(resourceName)
      // Open the stream last to avoid any handle leak
      inputStream <- LiftRules.getResource(resourceName).map(_.openStream())
    } yield StreamingResponse(
      data = inputStream,
      onEnd = () => inputStream.close(),
      size = resourceLength,
      headers = List("Content-type" -> mimeType),
      cookies = Nil,
      code = 200)
  }

  // Do not serve .bat, .htaccess, .old, etc. files
  private val mimeTypes = Map(
    "doc" -> "application/msword",
    "html" -> "text/html",
    "kml" -> "application/vnd.google-earth.kml+xml",
    "kmz" -> "application/vnd.google-earth.kmz",
    "mp3" -> "audio/mpeg3",
    "pdf" -> "application/pdf",
    "png" -> "image/png",
    "rss" -> "application/rss+xml",
    "xml" -> "application/xml",
    "xsl" -> "application/xml")

  // All files from old site are located in "/old_site"
  private def resourceNameFromReq(req: Req): String =
    "/old_site/"+
    req.path.partPath.mkString("/")+
    Some(req.path.suffix).filter(_.trim.nonEmpty).map("."+_).getOrElse("")

  private def resourceLength(resourceName: String): Box[Long] =
    LiftRules.getResource(resourceName) map { url =>
      val inputStream = url.openStream()
      val data = new Array[Byte](4096)
      // Use read and not skip as the latter doesn't work
      val streamLength = Iterator.continually(inputStream.read(data).toLong).takeWhile(_ > 0L).fold(0L)(_ + _)
      inputStream.close()
      streamLength
    }
}
