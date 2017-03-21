package eu.xeli.aquariumPI.gpio

import jpigpio.JPigpio

class Pwm(server: Server, pin: Int) {
  val pigpio = Pigpio.getInstance(server)

  pigpio.gpioSetMode(pin, JPigpio.PI_OUTPUT)
  pigpio.setPWMRange(pin, 250)
  pigpio.setPWMFrequency(pin, 100)

  def set(percentage: Double) {
    val dutycycle = Math.round(250 * (percentage / 100.0)).toInt
    pigpio.setPWMDutycycle(pin, dutycycle)
  }
}
