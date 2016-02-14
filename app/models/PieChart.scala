package models

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
            otherValuesLabel: String = "Other"): PieChart =
    PieChart(
      id = id,
      anchor = anchor,
      title = title,
      entityName = entityName,
      valueUnit = valueUnit,
      values =
          minimalValuesSet(values, maxValues, otherValuesLabel).zipWithIndex.map { case ((value, label), index) =>
            PieChartValue(value, label, color(index), lighterColor(index))
          })

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

  def color(index: Int): Color = colors.apply(index % colors.size)
  def lighterColor(index: Int): Color = lighterColors.apply(index % lighterColors.size)

  val colors = Seq(
    Color("F15854"), // Red
    Color("60BD68"), // Green
    Color("5DA5DA"), // Blue
    Color("FAA43A"), // Orange
    Color("B2912F"), // Brown
    Color("B276B2"), // Purple
    Color("DECF3F"), // Yellow
    Color("4D4D4D"), // Gray
    Color("F17CB0")) // Pink

  val lighterColors = colors.map(_.lighterOrDarker(factor = 1.1))
}

case class Color(red: Int, green: Int, blue: Int) {
  assert(red >= 0 && red <= 255)
  assert(green >= 0 && green <= 255)
  assert(blue >= 0 && blue <= 255)

  def html: String = f"#$red%02X$green%02X$blue%02X"

  def lighterOrDarker(factor: Double): Color =
    Color(
      red = Color.lighterOrDarker(red, factor),
      green = Color.lighterOrDarker(green, factor),
      blue = Color.lighterOrDarker(blue, factor))
}

object Color {
  def apply(html: String): Color = {
    val withoutSharpSymbol = if (html.startsWith("#")) html.substring(1) else html

    assert(withoutSharpSymbol.length == 6)

    Color(
      red = Integer.parseInt(withoutSharpSymbol.substring(0, 2), 16),
      green = Integer.parseInt(withoutSharpSymbol.substring(2, 4), 16),
      blue = Integer.parseInt(withoutSharpSymbol.substring(4, 6), 16))
  }

  def lighterOrDarker(colorValue: Int, factor: Double): Int = {
    assert(colorValue >= 0 && colorValue <= 255)
    (colorValue.toDouble * factor).round.toInt.max(0).min(255)
  }
}
