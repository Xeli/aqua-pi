package eu.xeli.aquariumPI.components.timer

import eu.xeli.aquariumPI.Controllee
import eu.xeli.aquariumPI.Moments

import java.time._
import scala.concurrent.duration._

class TimerCalculation(priority: Int, var pattern: Pattern) extends Controllee {
  private[this] var frequency = 5
  private[this] var moments:Moments = null

  update(pattern)

  def getPriority(): Int = priority
  def getFrequency(): Int = frequency

  def update(newPattern: Pattern) {
    pattern = newPattern
    newPattern match {
      case timePattern: TimePattern => {
        moments = new Moments(timePattern.pattern)
        frequency = 5
      }
      case intervalPattern: IntervalPattern => {
        frequency = intervalPattern.intervalDuration.toSeconds.toInt
      }
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
