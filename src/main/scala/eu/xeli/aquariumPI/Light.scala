package eu.xeli.aquariumPI

import gpio._
import java.time._

/*
 *
 * This class drives the lights
 *
 * Lights consist of 2 units with each 2 channels (white/blue)
 *
 * blue will be used as moonlight
 *
 */
class Light(bluePins: List[Int], whitePins: List[Int], blue: LightCalculation, white: LightCalculation) {

  val bluePWMs = bluePins.map(PWM)
  val whitePWMs = whitePins.map(PWM)

  def updateValues(time: LocalTime) {
    val blueValue = blue.getValue(time)
    val whiteValue = white.getValue(time)

    bluePWMs.map(_.set(blueValue))
    whitePWMs.map(_.set(blueValue))
  }
}
