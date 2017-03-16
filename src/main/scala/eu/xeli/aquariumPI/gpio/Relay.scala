package eu.xeli.aquariumPI.gpio

import eu.xeli.aquariumPI.gpio._
import jpigpio._

class Relay(server: Server, pin: Int, hasInverseHardware: Boolean = false) {
  val pigpio = Pigpio.getInstance(server)
  pigpio.gpioSetMode(pin, JPigpio.PI_OUTPUT)

  private def setRelay(state: Boolean) {
    if (state) {
      pigpio.gpioWrite(pin, JPigpio.PI_LOW)
    } else {
      pigpio.gpioWrite(pin, JPigpio.PI_HIGH)
    }
  }

  def set(state: Boolean) {
    if (hasInverseHardware) {
      setRelay(!state)
    } else {
      setRelay(state)
    }
  }

  def enable() {
    set(true)
  }

  def disable() {
    set(false)
  }
}
