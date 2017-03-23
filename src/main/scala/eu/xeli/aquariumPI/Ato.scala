package eu.xeli.aquariumPI

import gpio.{Relay, Listener}
import java.time.{Duration}

class Ato(servers: Servers, waterLevelSensorPin: Int, pumpPin: Int) {
  @volatile
  var waterLevel = 0.0

  val criticalWaterLevel = 0

  val pump = new Relay(servers.pigpio, pumpPin, true)

  val steady = Duration.ofNanos(3e+8.toInt)
  val listener = new Listener(servers.pigpio, waterLevelSensorPin, steady, handleChange)

  def handleChange = (level: Double) => {
    waterLevel = level
    if (level > 0) {
      pump.enable()
    } else {
      pump.disable()
    }
  }
}
