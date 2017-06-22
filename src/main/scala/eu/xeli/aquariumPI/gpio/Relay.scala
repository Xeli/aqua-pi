package eu.xeli.aquariumPI.gpio

import eu.xeli.aquariumPI.Output
import eu.xeli.aquariumPI.gpio._
import eu.xeli.aquariumPI.Server
import eu.xeli.jpigpio._

import com.typesafe.scalalogging.Logger

class Relay(pigpio: JPigpio, pin: Int, hasInverseHardware: Boolean = false) extends Output {
  val logger = Logger[Relay]

  logger.debug(s"Initializing relay on pin $pin")
  pigpio.gpioSetMode(pin, JPigpio.PI_OUTPUT)

  def enable() = set(true)
  def disable() = set(false)
  def setValue(value: Double) = set(value > 0.5)

  private def setRelay(state: Boolean) {
    logger.debug(s"Setting relay on pin $pin to " + (if (state) "low" else "high"))
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
