package eu.xeli.aquariumPI.gpio

import eu.xeli.aquariumPI.gpio._
import eu.xeli.aquariumPI.Server
import jpigpio._

class Relay(pigpio: JPigpio, pin: Int, hasInverseHardware: Boolean = false) {
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
