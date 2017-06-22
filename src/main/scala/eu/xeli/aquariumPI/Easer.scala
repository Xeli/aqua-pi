package eu.xeli.aquariumPI

import java.time._
import java.time.temporal.ChronoUnit

/*
 * Helper class to ease a value.
 *
 * Example:
 *   secondsToTransition = 5 (milliseconds)
 *   startTime           = 500 (milliseconds from unix epoch)
 *   maxOffset           = 10.0
 *   startValue          = 200.0
 *   endValue            = 310.0
 *
 *   this will mean if you call the getValue function once per second the returned values will be:
 *
 *   getValue(500) = 200.0
 *   getValue(501) = 222.0
 *   getValue(502) = 244.0
 *   getValue(503) = 266.0
 *   getValue(504) = 288.0
 *   getValue(505) = 310.0
 *
 */
case class Easer(duration: Duration, startTime: LocalTime, startValue: Double, endValue: Double) {
  private[this] val tolerance = 0.1

  def getValue(time: LocalTime): Double = {
    val differenceInSeconds:Double = ChronoUnit.SECONDS.between(startTime, time)
    val percentage = differenceInSeconds / duration.getSeconds
    startValue + ((endValue - startValue) * Math.min(percentage, 1))
  }

  def isFinished(currentValue: Double): Boolean = {
    Math.abs(currentValue - endValue) < MagicValues.DOUBLE_TOLERANCE
  }
}
