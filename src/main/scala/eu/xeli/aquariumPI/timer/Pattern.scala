package eu.xeli.aquariumPI.timer

import java.time.LocalTime
import scala.concurrent.duration._

sealed trait Pattern
case class TimePattern(name: String, pattern: List[(LocalTime, Double)], relayId: Int) extends Pattern
case class IntervalPattern(name: String, intervalDuration: Duration, startTime: LocalTime, relayId: Int) extends Pattern
