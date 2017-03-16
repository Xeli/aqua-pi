package eu.xeli.aquariumPI

import gpio.{Relay, Server, Listener}

class Ato(server: Server, waterLevelSensorPin: Int, pumpPin: Int) {
  val criticalWaterLevel = 0

  val pump = new Relay(server, pumpPin)
  val listener = new Listener(server, waterLevelSensorPin, handleChange)

  def handleChange = (level: Double) => {
    if (level > 0) {
      pump.enable()
    } else {
      pump.disable()
    }
  }
}
