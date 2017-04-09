package eu.xeli.aquariumPI.gpio

import eu.xeli.aquariumPI.{Output, Server}

class PwmGroup(server: Server, pins: List[Int]) extends Output {
  val pwms = pins.map(new Pwm(server, _))

  def setValue(percentage: Double) {
    pwms.map(_.set(percentage))
  }

  //we want to get the value from the rpi
  //eventhough there might be multiple pwms with different values in this group
  //we will assume they are all equal, if there's no pwm in this group return 0
  def getValue(): Double = {
    pwms.headOption match {
      case Some(pwm) => pwm.getValue()
      case None      => 0.0
    }
  }
}
