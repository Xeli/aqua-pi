package eu.xeli.aquariumPI

trait Controllee extends Ordered[Controllee] {
  var activated = true

  def getValue(): Double
  def getPriority(): Int
  def getFrequency(): Int

  def compare(a: Controllee): Int = {
    getPriority().compare(a.getPriority)
  }
}
