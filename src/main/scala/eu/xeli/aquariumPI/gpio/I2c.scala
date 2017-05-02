package eu.xeli.aquariumPI.gpio

import eu.xeli.aquariumPI.Server
import jpigpio.JPigpio

class I2c(server: Server, address: Int, bus: Int) {
  val pigpio = Pigpio.getInstance(server)
  val handle = pigpio.i2cOpen(bus, address)

  def getValue(numberOfBytes: Int): Double = {
    val data: Array[Byte] = new Array(2)
    val returnCode = pigpio.i2cReadDevice(handle, data)

    val Array(firstByte, secondByte) = data
    val rawValue = firstByte << 8 + secondByte

    rawValue
  }
}
