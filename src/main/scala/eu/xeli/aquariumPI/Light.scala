package eu.xeli.aquariumPI

import gpio._
import gpio.Pwm
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
class Light(server: Server,
            bluePins: List[Int], whitePins: List[Int],
            blue: LightCalculation, white: LightCalculation) extends Runnable {

  val bluePWMs = bluePins.map(new Pwm(server, _))
  val whitePWMs = whitePins.map(new Pwm(server, _))

  def run() {
    val zone = ZoneId.of("Europe/Amsterdam")
    val time = LocalTime.now(zone)

    val blueValue = blue.getValue(time)
    val whiteValue = white.getValue(time)

    bluePWMs.map(_.set(blueValue))
    whitePWMs.map(_.set(whiteValue))
    println("updating lights: (" + blueValue + "," + whiteValue + ")")
  }
}
