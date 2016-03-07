package models.statistics

case class PieChartValue[T](value: T, label: String, color: Color, lighterColor: Color)(implicit ev: Numeric[T])

case class PieChart[T](id: String,
                       anchor: String,
                       title: String,
                       entityName: String,
                       valueUnit: String,
                       values: Seq[PieChartValue[T]])(implicit ev: Numeric[T])

object PieChart {
  def apply[T](id: String,
               anchor: String,
               title: String,
               entityName: String,
               valueUnit: String,
               values: Seq[(T, String)],
               threshold: Option[T],
               otherValuesLabel: String = "Other")(implicit ev: Numeric[T]): PieChart[T] = {
    val minimalValuesSet = threshold.map(this.minimalValuesSet(values, _, otherValuesLabel)).getOrElse(values)

    val colors = Color.colors(count = minimalValuesSet.size)

    PieChart(
      id = id,
      anchor = anchor,
      title = title,
      entityName = entityName,
      valueUnit = valueUnit,
      values =
        minimalValuesSet.zipWithIndex.map { case ((value, label), index) =>
          PieChartValue(value, label, colors(index), colors(index).lighterOrDarker(LighterOrDarkerFactor))
        })
  }

  private def minimalValuesSet[T](values: Seq[(T, String)],
                                  threshold: T,
                                  otherValuesLabel: String)(implicit num: Numeric[T]): Seq[(T, String)] = {
    val (valuesToKeep, valuesToAggregate) = values.partition(value => num.gteq(value._1, threshold))

    if (valuesToAggregate.isEmpty)
      values
    else
      valuesToKeep :+ valuesToAggregate.map(_._1).sum -> otherValuesLabel
  }

  private val LighterOrDarkerFactor = 1.1
}
