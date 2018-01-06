package eu.xeli.aquariumPI.timer

import java.time.LocalTime
import scala.concurrent.duration._

sealed trait Pattern
case class TimePattern(pattern: List[(LocalTime, Double)]) extends Pattern
case class IntervalPattern(intervalDuration: Duration, startTime: LocalTime) extends Pattern
