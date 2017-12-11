package models.statistics

case class Color(red: Int, green: Int, blue: Int) {
  assert(red >= 0 && red <= 255)
  assert(green >= 0 && green <= 255)
  assert(blue >= 0 && blue <= 255)

  def html: String = f"#$red%02X$green%02X$blue%02X"

  def lighterOrDarker(factor: Double): Color =
    Color(red = Color.lighterOrDarker(red, factor),
          green = Color.lighterOrDarker(green, factor),
          blue = Color.lighterOrDarker(blue, factor))

  def distance(that: Color): Double =
    math.sqrt(
      math.pow(this.red - that.red, 2.0) +
        math.pow(this.green - that.green, 2.0) +
        math.pow(this.blue - that.blue, 2.0))
}

object Color {
  def apply(html: String): Color = {
    val withoutSharpSymbol = if (html.startsWith("#")) html.substring(1) else html

    assert(withoutSharpSymbol.length == 6)

    Color(
      red = Integer.parseInt(withoutSharpSymbol.substring(0, 2), 16),
      green = Integer.parseInt(withoutSharpSymbol.substring(2, 4), 16),
      blue = Integer.parseInt(withoutSharpSymbol.substring(4, 6), 16)
    )
  }

  def colors(count: Int): Seq[Color] =
    algorithm1Colors(count = count)
  //algorithm2Colors(baseColor = Color("5DA5DA"), count = count)
  //algorithm3Colors(baseColor = Color("5DA5DA"), count = count)

  private def lighterOrDarker(colorValue: Int, factor: Double): Int = {
    assert(colorValue >= 0 && colorValue <= 255)
    (colorValue.toDouble * factor).round.toInt.max(0).min(255)
  }

  private def algorithm1Colors(count: Int): Seq[Color] =
    for {
      i <- 0 until count
      last = i == count - 1
      baseColorIndex = i % fixedPalette.size
      // Prevent case where first and last colors are the same, as they are next to each other in a pie chart
      colorIndex = if (last && baseColorIndex == 0) baseColorIndex + 1 else baseColorIndex
    } yield fixedPalette(colorIndex)

  val Blue = Color("5DA5DA")
  val Brown = Color("B2912F")
  val Gray = Color("C0C0C0")
  val Green = Color("60BD68")
  val Orange = Color("FAA43A")
  val Pink = Color("F17CB0")
  val Purple = Color("B276B2")
  val Red = Color("F15854")
  val Yellow = Color("DECF3F")

  private val fixedPalette = Seq(Red, Green, Blue, Orange, Brown, Purple, Yellow, Gray, Pink)

  private def algorithm2Colors(baseColor: Color, count: Int): Seq[Color] = {
    val orderedColors = this.orderedColors(baseColor, count)

    def mostDistant(color: Color, otherColors: Seq[Color]): Color =
      otherColors.maxBy(color.distance)

    @annotation.tailrec
    def contrastedColors(acc: List[Color], remaining: List[Color]): List[Color] =
      if (remaining.isEmpty) {
        acc.reverse
      } else {
        val lastAddedColor = acc.head
        val mostDistantInRemaining = mostDistant(lastAddedColor, remaining)
        contrastedColors(mostDistantInRemaining :: acc,
                         remaining.filterNot(_ == mostDistantInRemaining))
      }

    orderedColors.toList match {
      case firstColor :: remainingColors =>
        contrastedColors(acc = List(firstColor), remainingColors)
      case Nil =>
        Seq()
    }
  }

  private def orderedColors(baseColor: Color, count: Int): Seq[Color] = {
    import java.awt.{Color => AwtColor}

    val baseHue :: baseSaturation :: baseBrightness :: Nil =
      AwtColor.RGBtoHSB(baseColor.red, baseColor.green, baseColor.blue, null).toList

    //println(s"baseHue=$baseHue, baseSaturation=$baseSaturation, baseBrightness=$baseBrightness")

    for {
      index <- 0 until count
      hue = (baseHue.toDouble + index.toDouble / count.toDouble * 3.0 / 3.0) % 1.0
      //_ = { println(s" hue = $hue")}
      rgb = AwtColor.HSBtoRGB(hue.toFloat, baseSaturation, baseBrightness)
      awtColor = new AwtColor(rgb)
    } yield Color(awtColor.getRed, awtColor.getGreen, awtColor.getBlue)
  }

  private def algorithm3Colors(baseColor: Color, count: Int): Seq[Color] = {
    /*
    Idea:
    - choose randomly count colors, with a constraint on the luminosity (Lmin < L < Lmax)
    - arrange those count colors in a given order
     * start with the base color (baseColor)
     * choose each following color trying to maximize the sum of the distances with all the colors that have already
         been chosen (maybe with a factor to decrease the importance of the more distance colors)
    - evaluate the color arrangement using the total sum of the distances between two consecutive colors
    - repeat several times
    - chose the color arrangement that maximize the sum of the distances
     */
    // @todo
    Seq()
  }
}
