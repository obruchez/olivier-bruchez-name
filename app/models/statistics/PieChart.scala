package models.statistics

case class PieChartValue(value: Double, label: String, color: Color, lighterColor: Color)

case class PieChart(id: String,
                    anchor: String,
                    title: String,
                    entityName: String,
                    valueUnit: String,
                    values: Seq[PieChartValue])

object PieChart {
  def apply(id: String,
            anchor: String,
            title: String,
            entityName: String,
            valueUnit: String,
            values: Seq[(Double, String)],
            maxValues: Int = 10,
            otherValuesLabel: String = "Other"): PieChart = {
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

  private def minimalValuesSet(values: Seq[(Double, String)],
                               maxValues: Int,
                               otherValuesLabel: String): Seq[(Double, String)] =
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
