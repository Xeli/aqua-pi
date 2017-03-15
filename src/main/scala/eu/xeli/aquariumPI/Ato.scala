package eu.xeli.aquariumPI

import eu.xeli.aquariumPI.Relay
import eu.xeli.aquariumPI.sensors.WaterLevel

class Ato(waterLevelSensorPin: Int, pumpPin: Int) {
  val criticalWaterLevel = 0

  val pump = new Relay(pumpPin)
  val sensor = new WaterLevel(waterLevelSensorPin)

  def apply() {
    val waterLevel = sensor.getValue()

    if (waterLevel < criticalWaterLevel) {
      pump.enable()
      while (sensor.getValue() < criticalWaterLevel) {
        Thread.sleep(5000)
      }
      pump.disable()
    }
  }
}
