package eu.xeli.aquariumPI.gpio

import jpigpio.{Alert, JPigpio}

class Listener(server: Server, pin: Int, function: Double => Unit) {
  val pigpio = Pigpio.getInstance(server)

  val alert = new Alert {
    def alert(pin: Int, level: Int, tick: Long) {
      function(level)
    }
  }

  pigpio.gpioSetPullUpDown(pin, JPigpio.PI_PUD_UP)
  pigpio.gpioSetAlertFunc(pin, alert)
}
