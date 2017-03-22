package eu.xeli.aquariumPI

import gpio.{Relay, Server, Listener}
import java.time.{Duration}

class Ato(server: Server, waterLevelSensorPin: Int, pumpPin: Int) {
  val criticalWaterLevel = 0

  val pump = new Relay(server, pumpPin, true)

  val steady = Duration.ofNanos(3e+8.toInt)
  val listener = new Listener(server, waterLevelSensorPin, steady, handleChange)

  def handleChange = (level: Double) => {
    if (level > 0) {
      pump.enable()
    } else {
      pump.disable()
    }
  }
}
