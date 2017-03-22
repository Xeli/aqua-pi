package eu.xeli.aquariumPI.gpio

import jpigpio.{Alert, JPigpio}
import java.time.Duration
import java.time.temporal.ChronoUnit

class Listener(server: Server, pin: Int, steady: Duration, function: Double => Unit) {
  val pigpio = Pigpio.getInstance(server)

  val alert = new Alert {
    def alert(pin: Int, level: Int, tick: Long) {
      function(level)
    }
  }

  pigpio.gpioSetPullUpDown(pin, JPigpio.PI_PUD_UP)

  val micros = (steady.getSeconds() * 1e+6) + (steady.getNano() / 1000);
  pigpio.gpioGlitchFilter(pin, micros.toInt)
  pigpio.gpioSetAlertFunc(pin, alert)
}
