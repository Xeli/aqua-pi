package eu.xeli.aquariumPI.light

import eu.xeli.aquariumPI.Moments

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
class LightCalculation(priority: Int, var pattern: LightPattern) extends Controllee {
  var moments = new Moments(pattern)

  def getPriority(): Int = priority
  def getFrequency(): Int = 5

  def update(newPattern: Seq[(LocalTime, Double)]) = {
    moments = new Moments(newPattern)
    pattern = newPattern
  }

  def getValue(): Double = {
    val zone = ZoneId.of("Europe/Amsterdam")
    val time = LocalTime.now(zone)

    moments.getValue(time)
  }

}

object LightCalculation {
  type LightPattern = Seq[(LocalTime, Double)]
}
