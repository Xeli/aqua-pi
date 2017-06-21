package eu.xeli.aquariumPI

import org.scalatest._
import java.time._

class MomentsSpec extends FlatSpec with Matchers {
  "A moments object" should "give back correct values based on a given pattern" in {
    val pattern:Seq[(LocalTime, Double)] = Seq(
      (LocalTime.parse("10:00"), 0),
      (LocalTime.parse("11:00"), 100),
      (LocalTime.parse("12:00"), 100),
      (LocalTime.parse("13:00"), 0)
    )
    val moments = new Moments(pattern)

    moments.getValue(LocalTime.parse("10:00")) should be (0)
    moments.getValue(LocalTime.parse("10:30")) should be (50)
    moments.getValue(LocalTime.parse("11:00")) should be (100)
    moments.getValue(LocalTime.parse("12:00")) should be (100)
    moments.getValue(LocalTime.parse("12:15")) should be (75)
    moments.getValue(LocalTime.parse("12:45")) should be (25)
  }
}
