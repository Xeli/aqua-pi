package eu.xeli.aquariumPI

abstract class Controllee(val priority: Int) extends Ordered[Controllee] {
  var activated = true

  def getValue(): Double
  def getFrequency(): Int
  def compare(a: Controllee): Int = {
    priority.compare(a.priority)
  }
}
