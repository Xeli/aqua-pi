package eu.xeli.aquariumPI

import java.time._
import java.time.temporal.ChronoUnit

class Moments(pattern: Seq[(LocalTime, Double)]) {
  val sections:Seq[Section] = convertToSections(pattern)

  case class Moment(time: LocalTime, value: Double)
  case class Section(from: Moment, to: Moment) {
    val durationInSeconds = from.time.until(to.time, ChronoUnit.SECONDS)
    def intersects(time: LocalTime): Boolean = {
      time.isAfter(from.time) && (time.isBefore(to.time) || time.equals(to.time))
    }

    def getValue(time: LocalTime): Double = {
      val secondsAfterFrom: Double = from.time.until(time, ChronoUnit.SECONDS)
      val percentage: Double = secondsAfterFrom / durationInSeconds

      from.value + ( (to.value - from.value) * percentage )
    }
  }

  private[this] def convertToSections(pattern: Seq[(LocalTime, Double)]): Seq[Section] = {
    val moments = pattern.map({ case (time, value) => Moment(time, value) })
    val momentTuples = moments zip moments.tail
    momentTuples.map({ case (x,y) => Section(x,y)})
  }

  def getValue(time: LocalTime, default: Double = 0): Double = {
    val maybeSection = sections.find(_.intersects(time))
    maybeSection match {
      case Some(section) =>
        section.getValue(time)
      case None =>
        default
    }
  }
}
