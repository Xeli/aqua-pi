package eu.xeli.aquariumPI.gpio

import eu.xeli.aquariumPI.Server
import jpigpio.{JPigpio, PigpioException, PigpioSocket, Utils}

object Pigpio {

  def getInstance(server: Server): JPigpio = {
    val pigpio = new PigpioSocket(server.host, server.port)
    pigpio.gpioInitialize()
    pigpio
  }
}
