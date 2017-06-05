package eu.xeli.aquariumPI.gpio

import eu.xeli.aquariumPI.Server
import eu.xeli.jpigpio.JPigpio
import eu.xeli.jpigpio.PigpioException

class Pwm(pigpio: JPigpio, pin: Int) {
  val Range = 250
  val Frequency = 100

  pigpio.gpioSetMode(pin, JPigpio.PI_OUTPUT)
  pigpio.setPWMRange(pin, Range)
  pigpio.setPWMFrequency(pin, Frequency)

  def set(percentage: Double) {
    val dutycycle = Math.round(Range * (percentage / Frequency)).toInt
    pigpio.setPWMDutycycle(pin, dutycycle)
  }

  def getValue(): Double = {
    try {
      val dutycycle = pigpio.getPWMDutycycle(pin)
      (dutycycle / 250.0) * 100.0
    } catch {
      case pie: PigpioException => 0.0
    }
  }
}
