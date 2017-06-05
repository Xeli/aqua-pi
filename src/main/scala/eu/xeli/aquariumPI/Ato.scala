package eu.xeli.aquariumPI

import gpio.{Relay, Listener}

import java.time.{Duration}
import eu.xeli.jpigpio.JPigpio

class Ato(pigpio: JPigpio, waterLevelSensorPin: Int, pumpPin: Int) {
  @volatile
  var waterLevel = 0.0

  val criticalWaterLevel = 0

  val pump = new Relay(pigpio, pumpPin, true)

  val steady = Duration.ofNanos(3e+8.toInt)
  val listener = new Listener(pigpio, waterLevelSensorPin, steady, handleChange)

  def handleChange = (level: Double) => {
    waterLevel = level
    if (level > 0) {
      pump.enable()
    } else {
      pump.disable()
    }
  }
}
