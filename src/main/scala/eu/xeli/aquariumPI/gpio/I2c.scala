package eu.xeli.aquariumPI.gpio

import eu.xeli.jpigpio.JPigpio

class I2c(pigpio: JPigpio, address: Int, bus: Int) {
  val handle = pigpio.i2cOpen(bus, address)

  def getValue(numberOfBytes: Int): Double = {
    val data: Array[Byte] = new Array(2)
    val returnCode = pigpio.i2cReadDevice(handle, data)

    val Array(firstByte, secondByte) = data
    val rawValue = firstByte << 8 + secondByte

    rawValue
  }
}
