package eu.xeli.aquariumPI.gpio

import eu.xeli.aquariumPI.Server
import eu.xeli.jpigpio.{Alert, JPigpio}
import java.time.Duration
import java.time.temporal.ChronoUnit

class Listener(pigpio: JPigpio, pin: Int, steady: Duration, function: Double => Unit) {
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
