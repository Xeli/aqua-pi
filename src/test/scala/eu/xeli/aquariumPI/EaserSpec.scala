package eu.xeli.aquariumPI

import org.scalatest._
import org.scalamock.scalatest.MockFactory

import java.time._
import java.util.concurrent._

class EaserSpec extends FlatSpec with Matchers {

  "An easer" should "be finished when the current value equals the target value" in {
    val easer = new Easer(duration = Duration.ofSeconds(10), startTime = LocalTime.parse("10:00:00"),
                          startValue = 100.0, endValue = 200.0)

    easer.isFinished(200.0) should be (true)
    easer.isFinished(190.0) should be (false)
    easer.isFinished(210.0) should be (false)
  }

  it should "ease values when value is requested before transition time is over" in {
    val easer = new Easer(duration = Duration.ofSeconds(10), startTime = LocalTime.parse("10:00:00"),
                          startValue = 100.0, endValue = 200.0)

    easer.getValue(LocalTime.parse("10:00:00")) should be (100.0)
    easer.getValue(LocalTime.parse("10:00:05")) should be (150.0)
    easer.getValue(LocalTime.parse("10:00:10")) should be (200.0)
  }
}
