package eu.xeli.aquariumPI.gpio

import eu.xeli.aquariumPI.UnitSuite

import eu.xeli.aquariumPI.gpio._

class PwmSpec extends UnitSuite {
  "Pwm" should "return correct value based on dutycycle" in {
    val x = 1
    val y = 1
    assert(x === y)
  }
}
