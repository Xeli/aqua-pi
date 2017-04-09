package eu.xeli.aquariumPI

trait Output {
  def setValue(value: Double)

  def getValue(): Double
}
