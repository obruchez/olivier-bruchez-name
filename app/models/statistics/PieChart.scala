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
               maxValues: Int = 10,
               otherValuesLabel: String = "Other")(implicit ev: Numeric[T]): PieChart[T] = {
    val minimalValuesSet = this.minimalValuesSet(values, maxValues, otherValuesLabel)
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
                                  maxValues: Int,
                                  otherValuesLabel: String)(implicit ev: Numeric[T]): Seq[(T, String)] =
    if (values.size <= maxValues) {
      values
    } else {
      val valuesToKeep = values.take(maxValues - 1)
      val valuesToAggregate = values.drop(maxValues -1)

      val aggregatedValue = valuesToAggregate.map(_._1).sum -> otherValuesLabel

      valuesToKeep :+ aggregatedValue
    }

  private val LighterOrDarkerFactor = 1.1
}
