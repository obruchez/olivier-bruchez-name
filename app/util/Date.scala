package util

import java.text.DateFormatSymbols
import java.util.Locale

import org.joda.time._

import scala.util._

object Date {
  def partialFromYyyymmddString(string: String): Try[Partial] = Try {
    // scalastyle:off magic.number
    val year = Try(string.substring(0, 4).toInt).map(DateTimeFieldType.year() -> _)
    val month = Try(string.substring(5, 7).toInt).map(DateTimeFieldType.monthOfYear() -> _)
    val day = Try(string.substring(8, 10).toInt).map(DateTimeFieldType.dayOfMonth() -> _)
    // scalastyle:on magic.number

    val validFields = Seq(year, month, day).flatMap(_.toOption)

    new Partial(validFields.map(_._1).toArray, validFields.map(_._2).toArray)
  }

  def intervalEnglishString(from: ReadablePartial, to: ReadablePartial): String =
    from.intervalEnglishString(to)

  implicit class PartialOps(readablePartial: ReadablePartial) {
    def yyyymmddString: String =
      DateTimeFields(readablePartial).yyyymmddString

    // @todo make this method shorter and more readable
    def intervalEnglishString(to: ReadablePartial): String = {
      val fromDateTimeFields = DateTimeFields(readablePartial)
      val toDateTimeFields = DateTimeFields(to)

      if (
        fromDateTimeFields.yyyymmddValuesAllDefined && toDateTimeFields.yyyymmddValuesAllDefined
      ) {
        if (fromDateTimeFields.year == toDateTimeFields.year) {
          if (fromDateTimeFields.month == toDateTimeFields.month) {
            if (fromDateTimeFields.dayOfMonth == toDateTimeFields.dayOfMonth) {
              s"${fromDateTimeFields.monthEnglishString} " +
                s"${fromDateTimeFields.dayOfMonthString}, " +
                s"${fromDateTimeFields.yearString}"
            } else {
              s"${fromDateTimeFields.monthEnglishString} " +
                s"${fromDateTimeFields.dayOfMonthString}-${toDateTimeFields.dayOfMonthString}, " +
                s"${fromDateTimeFields.yearString}"
            }
          } else {
            s"${fromDateTimeFields.monthEnglishString} ${fromDateTimeFields.dayOfMonthString}-" +
              s"${toDateTimeFields.monthEnglishString} ${toDateTimeFields.dayOfMonthString}, " +
              s"${fromDateTimeFields.yearString}"
          }
        } else {
          s"${fromDateTimeFields.monthEnglishString} " +
            s"${fromDateTimeFields.dayOfMonthString}, " +
            s"${fromDateTimeFields.yearString}-" +
            s"${toDateTimeFields.monthEnglishString} " +
            s"${toDateTimeFields.dayOfMonthString}, " +
            s"${toDateTimeFields.yearString}"
        }
      } else {
        val fromString = fromDateTimeFields.yyyymmddString
        val toString = toDateTimeFields.yyyymmddString

        if (fromString == toString) fromString else s"$fromString-$toString"
      }
    }

    def year: Option[Int] =
      Try(readablePartial.get(DateTimeFieldType.year())).toOption

    def emptyDate: Boolean =
      readablePartial.size() == 0

    def futureDate: Boolean = {
      val now = new LocalDate

      val fieldsToCompare = List(
        DateTimeFieldType.year(),
        DateTimeFieldType.monthOfYear(),
        DateTimeFieldType.dayOfMonth()
      )

      def nowBefore(fieldsLeftToCompare: List[DateTimeFieldType]): Boolean =
        fieldsLeftToCompare match {
          case Nil =>
            // All fields equal
            false
          case field :: rest =>
            Try(readablePartial.get(field)) match {
              case Success(value) =>
                val nowValue = now.get(field)

                if (nowValue < value) {
                  true
                } else if (nowValue > value) {
                  false
                } else {
                  nowBefore(rest)
                }
              case Failure(_) =>
                // No more field available for comparison => consider the date as future (in that case, unknown/undetermined)
                true
            }
        }

      nowBefore(fieldsToCompare)
    }
  }
}

case class DateTimeFields(valuesByType: Map[DateTimeFieldType, Int]) {
  def year: Option[Int] =
    valuesByType.get(DateTimeFieldType.year())

  def month: Option[Int] =
    valuesByType.get(DateTimeFieldType.monthOfYear())

  def dayOfMonth: Option[Int] =
    valuesByType.get(DateTimeFieldType.dayOfMonth())

  def yyyymmddValuesAllDefined: Boolean =
    year.isDefined && month.isDefined && dayOfMonth.isDefined

  def yearString: String =
    year.fold("?")(_.toString)

  def paddedYearString: String =
    year.fold("????")(y => f"$y%04d")

  def monthString: String =
    month.fold("?")(_.toString)

  def paddedMonthString: String =
    month.fold("??")(m => f"$m%02d")

  def dayOfMonthString: String =
    dayOfMonth.fold("?")(_.toString)

  def paddedDayOfMonthString: String =
    dayOfMonth.fold("??")(d => f"$d%02d")

  def yyyymmddString: String =
    s"$paddedYearString-$paddedMonthString-$paddedDayOfMonthString"

  def monthEnglishString: String =
    month.fold("?")(m => DateTimeFields.dateFormatSymbols.getMonths.apply(m - 1))
}

object DateTimeFields {
  protected val dateFormatSymbols = new DateFormatSymbols(Locale.ENGLISH)

  def apply(readablePartial: ReadablePartial): DateTimeFields =
    DateTimeFields(Map((for {
      fieldIndex <- 0 until readablePartial.size
      dateTimeFieldType = readablePartial.getFieldType(fieldIndex)
      dateTimeFieldValue = readablePartial.getValue(fieldIndex)
    } yield dateTimeFieldType -> dateTimeFieldValue): _*))
}
