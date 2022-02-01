package models.statistics

case class BarOrLineChartValueSet[T](values: Seq[T], label: String, color: Color)

case class BarOrLineChart[T](
    baseId: String,
    title: String,
    labels: Seq[String],
    valueSets: Seq[BarOrLineChartValueSet[T]],
    minYValue: Option[T] = None,
    maxYValue: Option[T] = None
)(implicit ev: Numeric[T]) {
  val id = s"chart-$baseId"
  val anchor = baseId
}
