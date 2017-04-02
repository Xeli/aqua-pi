package eu.xeli.aquariumPI.light

import eu.xeli.aquariumPI.gpio._
import eu.xeli.aquariumPI.Servers
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
class Light(servers: Servers,
            bluePins: List[Int], whitePins: List[Int],
            blue: LightCalculation, white: LightCalculation) extends Runnable {

  var lastValue = LightMetric(0,0)

  val bluePWMs = bluePins.map(new Pwm(servers.pigpio, _))
  val whitePWMs = whitePins.map(new Pwm(servers.pigpio, _))

  def run() {
    val blueValue = blue.getValue()
    val whiteValue = white.getValue()

    //send to pigpio
    bluePWMs.map(_.set(blueValue))
    whitePWMs.map(_.set(whiteValue))

    lastValue = LightMetric(blueValue, whiteValue)

    println("updating lights: (" + blueValue + "," + whiteValue + ")")
  }
}
