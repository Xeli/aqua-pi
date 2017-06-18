package eu.xeli.aquariumPI.gpio

import eu.xeli.aquariumPI.Output
import eu.xeli.aquariumPI.gpio._
import eu.xeli.aquariumPI.Server
import eu.xeli.jpigpio._

class Relay(pigpio: JPigpio, pin: Int, hasInverseHardware: Boolean = false) extends Output {
  pigpio.gpioSetMode(pin, JPigpio.PI_OUTPUT)

  def enable() = set(true)
  def disable() = set(false)
  def setValue(value: Double) = set(value > 0.5)

  private def setRelay(state: Boolean) {
    if (state) {
      pigpio.gpioWrite(pin, JPigpio.PI_LOW)
    } else {
      pigpio.gpioWrite(pin, JPigpio.PI_HIGH)
    }
  }

  def getValue(): Double = {
    var isHigh = pigpio.gpioRead(pin)
    if (hasInverseHardware) {
      isHigh = !isHigh
    }

    if (isHigh) 1.0 else 0.0
  }

  def set(state: Boolean) {
    if (hasInverseHardware) {
      setRelay(!state)
    } else {
      setRelay(state)
    }
  }

}
