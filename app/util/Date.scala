package util

import org.joda.time._
import scala.util._

object Date {
  def partialFromYyyyMmDdString(string: String): Try[Partial] = Try {
    val year = Try(string.substring(0, 4).toInt).map(DateTimeFieldType.year() -> _)
    val month = Try(string.substring(5, 7).toInt).map(DateTimeFieldType.monthOfYear() -> _)
    val day = Try(string.substring(8, 10).toInt).map(DateTimeFieldType.dayOfMonth() -> _)

    val validFields = Seq(year, month, day).map(_.toOption).flatten

    new Partial(validFields.map(_._1).toArray, validFields.map(_._2).toArray)
  }
}
