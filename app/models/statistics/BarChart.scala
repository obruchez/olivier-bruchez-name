package models.statistics

case class BarChart[T](baseId: String,
                       title: String,
                       datasetLabel: String,
                       values: Seq[(String, T)])(implicit ev: Numeric[T]) {
  val id = s"chart-$baseId"
  val anchor = baseId
}
