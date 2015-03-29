package util

import org.joda.time._
import scala.util._

object Date {
  def partialFromYyyymmddString(string: String): Try[Partial] = Try {
    val year = Try(string.substring(0, 4).toInt).map(DateTimeFieldType.year() -> _)
    val month = Try(string.substring(5, 7).toInt).map(DateTimeFieldType.monthOfYear() -> _)
    val day = Try(string.substring(8, 10).toInt).map(DateTimeFieldType.dayOfMonth() -> _)

    val validFields = Seq(year, month, day).map(_.toOption).flatten

    new Partial(validFields.map(_._1).toArray, validFields.map(_._2).toArray)
  }

  implicit class PartialOps(partial: Partial) {
    def yyyymmddString: String = {
      val valuesByType = Map(partial.getFieldTypes.toSeq.map(fieldType => fieldType -> partial.get(fieldType)): _*)

      val year = valuesByType.get(DateTimeFieldType.year()).fold("????")(y => f"$y%04d")
      val month = valuesByType.get(DateTimeFieldType.monthOfYear()).fold("??")(m => f"$m%02d")
      val day = valuesByType.get(DateTimeFieldType.dayOfMonth()).fold("??")(d => f"$d%02d")

      s"$year-$month-$day"
    }

    def year: Option[Int] =
      Try(partial.get(DateTimeFieldType.year())).toOption
  }
}
