package eu.xeli.aquariumPI.light

import java.time._
import java.time.temporal.ChronoUnit
import eu.xeli.aquariumPI.Controllee
import LightCalculation._

/*
 *
 * This class calculates the light intensity based on a pattern.
 *
 * For instance you could say the lights pattern is:
 *   - 0% at 6 o'clock,
 *   - 100% at 12 o'clock
 *
 * If you ask for the light intensity at 9 o'clock it would give back 50%
 *
 */
class LightCalculation(priority: Int, var initialPattern: LightPattern) extends Controllee(priority) {
  var sections:Seq[Section] = Seq()

  case class Moment(time: LocalTime, value: Double)
  case class Section(from: Moment, to: Moment) {
    val durationInSeconds = from.time.until(to.time, ChronoUnit.SECONDS)
    def intersects(time: LocalTime): Boolean = {
      time.isAfter(from.time) && time.isBefore(to.time)
    }

    def getValue(time: LocalTime): Double = {
      val secondsAfterFrom = from.time.until(time, ChronoUnit.SECONDS)
      val percentage = secondsAfterFrom.toDouble / durationInSeconds

      from.value + ( (to.value - from.value) * percentage )
    }
  }

  //Convert a list of time and led intensity into List of sections
  def convert(data: LightPattern): Seq[Section] = {
    val moments = data.map({ case (time, value) => Moment(LocalTime.parse(time), value) })
    val momentTuples = moments zip moments.tail
    momentTuples.map({ case (x,y) => Section(x,y)})
  }

  def getValue(): Double = {
    val zone = ZoneId.of("Europe/Amsterdam")
    val time = LocalTime.now(zone)

    val maybeSection = sections.find(_.intersects(time))
    maybeSection match {
      case Some(section) =>
        section.getValue(time)
      case None =>
        0
    }
  }

  def getFrequency(): Int = {
    5
  }

  def setSections(pattern: LightPattern) {
    sections = convert(pattern)
  }

  setSections(initialPattern)
}

object LightCalculation {
  type LightPattern = Seq[(String, Double)]
}
