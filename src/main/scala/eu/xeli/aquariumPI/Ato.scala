package eu.xeli.aquariumPI

import eu.xeli.aquariumPI.sensors.WaterLevel

class Ato(waterLevelSensorPin: Int, pumpPin: Int) {
  def apply() {
    val waterLevel = new WaterLevel(waterLevelSensorPin).getValue()

    //TODO
  }
}
