package models.statistics

case class BarChartValueSet[T](values: Seq[T], color: Color)

case class BarChart[T](baseId: String,
                       title: String,
                       datasetLabel: String,
                       labels: Seq[String],
                       valueSets: Seq[BarChartValueSet[T]])(implicit ev: Numeric[T]) {
  val id = s"chart-$baseId"
  val anchor = baseId
}
