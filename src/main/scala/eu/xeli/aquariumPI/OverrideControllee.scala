package eu.xeli.aquariumPI

class OverrideControllee(priority: Int, key: String) extends Controllee with Runnable {
  activated = false
  var value = 0

  def getFrequency(): Int = 1
  def getValue(): Double = value
  def getPriority(): Int = 1

  def run() {
    println("Boost!")
    value = 80
    activated = true
  }
}
