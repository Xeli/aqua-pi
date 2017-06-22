package eu.xeli.aquariumPI.timer

import eu.xeli.aquariumPI.Controllee
import eu.xeli.aquariumPI.Moments

import java.time._
import scala.concurrent.duration._

class TimerCalculation(priority: Int, var pattern: Pattern) extends Controllee {
  var moments = pattern match {
    case timePattern: TimePattern => new Moments(timePattern.pattern)
    case _ => null
  }

  def getPriority(): Int = priority
  def getFrequency(): Int = 5

  def update(newPattern: Pattern) {
    newPattern match {
      case timePattern: TimePattern => {
        moments = new Moments(timePattern.pattern)
        pattern = newPattern
      }
      case intervalPattern: IntervalPattern => pattern = newPattern
    }
  }

  def getValue() : Double = {
    pattern match {
      case timePattern: TimePattern => getValue(timePattern)
      case intervalPattern: IntervalPattern => getValue(intervalPattern)
    }
  }

  def getValue(timePattern: TimePattern): Double = {
    val zone = ZoneId.of("Europe/Amsterdam")
    val time = LocalTime.now(zone)

    moments.getValue(time)
  }

  def getValue(intervalPattern: IntervalPattern): Double = {
    val durationInSeconds = intervalPattern.intervalDuration.toSeconds

    val zone = ZoneId.of("Europe/Amsterdam")
    val time = LocalTime.now(zone)
    val timeNowSeconds = time.toSecondOfDay
    val startTimeSeconds = intervalPattern.startTime.toSecondOfDay

    val insideInterval = Math.abs(Math.floor((timeNowSeconds - startTimeSeconds) / durationInSeconds).toInt % 2) == 0
    if (insideInterval) 1.0 else 0.0
  }
}
