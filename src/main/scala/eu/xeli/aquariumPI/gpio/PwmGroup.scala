package eu.xeli.aquariumPI.gpio

import eu.xeli.aquariumPI.{Output, Server}

class PwmGroup(server: Server, pins: List[Int]) extends Output {
  val pwms = pins.map(new Pwm(server, _))

  def setValue(percentage: Double) {
    pwms.map(_.set(percentage))
  }
}
