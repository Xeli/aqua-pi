package eu.xeli.aquariumPI.gpio

import eu.xeli.aquariumPI.Server
import eu.xeli.jpigpio.JPigpio
import eu.xeli.jpigpio.PigpioException

import com.typesafe.scalalogging.Logger

class Pwm(pigpio: JPigpio, pin: Int) {
  val logger = Logger[Pwm]
  val range = 250
  val frequency = 100

  logger.debug(s"Initialize PWM at pin: $pin with frequency: $frequency and range: $range")
  pigpio.gpioSetMode(pin, JPigpio.PI_OUTPUT)
  pigpio.setPWMRange(pin, range)
  pigpio.setPWMFrequency(pin, frequency)

  def set(percentage: Double) {
    val dutycycle = Math.round(range * (percentage / frequency)).toInt
    logger.debug(s"Setting PWM dutycycle at: $dutycycle")

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
