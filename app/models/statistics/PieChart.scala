package models.statistics

case class PieChartValue[T](value: T, label: String, color: Color, lighterColor: Color)(
    implicit ev: Numeric[T])

case class PieChart[T](baseId: String,
                       title: String,
                       entityName: String,
                       valueUnit: String,
                       values: Seq[PieChartValue[T]])(implicit ev: Numeric[T]) {
  val id = s"chart-$baseId"
  val anchor = baseId
}

object PieChart {
  def apply[T](baseId: String,
               title: String,
               entityName: String,
               valueUnit: String,
               values: Seq[(T, String)],
               threshold: Option[T] = None,
               maxValueCount: Option[Int] = None,
               otherValuesLabel: String = "Other")(implicit ev: Numeric[T]): PieChart[T] = {
    val minimalValuesSet = this.minimalValuesSet(values, threshold, maxValueCount, otherValuesLabel)

    val colors = Color.colors(count = minimalValuesSet.size)

    PieChart(
      baseId = baseId,
      title = title,
      entityName = entityName,
      valueUnit = valueUnit,
      values = minimalValuesSet.zipWithIndex.map {
        case ((value, label), index) =>
          PieChartValue(value,
                        label,
                        colors(index),
                        colors(index).lighterOrDarker(LighterOrDarkerFactor))
      }
    )
  }

  private def minimalValuesSet[T](
      values: Seq[(T, String)],
      thresholdOption: Option[T],
      maxValueCountOption: Option[Int],
      otherValuesLabel: String)(implicit num: Numeric[T]): Seq[(T, String)] = {
    val (valuesToKeepByThreshold, valuesToAggregateByThreshold) =
      thresholdOption
        .map(threshold => values.partition(value => num.gteq(value._1, threshold)))
        .getOrElse((values, Seq()))

    val (valuesToKeepByMaxValueCount, valuesToAggregateByMaxValueCount) =
      maxValueCountOption
        .map(maxValueCount => (values.take(maxValueCount - 1), values.drop(maxValueCount - 1)))
        .getOrElse((values, Seq()))

    val (valuesToKeep, valuesToAggregate) =
      if (valuesToKeepByThreshold.size < valuesToKeepByMaxValueCount.size)
        (valuesToKeepByThreshold, valuesToAggregateByThreshold)
      else
        (valuesToKeepByMaxValueCount, valuesToAggregateByMaxValueCount)

    if (valuesToAggregate.isEmpty)
      values
    else
      valuesToKeep :+ valuesToAggregate.map(_._1).sum -> otherValuesLabel
  }

  private val LighterOrDarkerFactor = 1.1
}
