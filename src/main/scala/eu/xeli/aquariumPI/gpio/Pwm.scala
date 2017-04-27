package eu.xeli.aquariumPI.gpio

import eu.xeli.aquariumPI.Server
import jpigpio.JPigpio
import jpigpio.PigpioException

class Pwm(server: Server, pin: Int) {
  val pigpio = Pigpio.getInstance(server)

  pigpio.gpioSetMode(pin, JPigpio.PI_OUTPUT)
  pigpio.setPWMRange(pin, 250)
  pigpio.setPWMFrequency(pin, 100)

  def set(percentage: Double) {
    val dutycycle = Math.round(250 * (percentage / 100.0)).toInt
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
