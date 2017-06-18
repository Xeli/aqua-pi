package eu.xeli.aquariumPI.timer

import eu.xeli.aquariumPI.Controller
import eu.xeli.aquariumPI.gpio.Relay
import eu.xeli.aquariumPI.config.pureconfig.TimeDoubleConverter
import eu.xeli.aquariumPI.config.InvalidConfigException

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}
import scala.collection.immutable.HashMap

import eu.xeli.jpigpio.JPigpio
import com.typesafe.config._
import pureconfig._
import pureconfig.configurable._
import pureconfig.error.ConfigReaderFailures

class Timer(pigpio: JPigpio, config: Config) {
  case class TimerRelay(name: String, pattern: Pattern, relay: Relay, calculator: TimerCalculation)

  /*
   * Config case-classes
   */
  case class TimerConfig(relays: List[TimerRelayConfig])
  case class TimerRelayConfig(name: String, relayId: Int, inverseRelay: Option[Boolean], pattern: Pattern)

  //initial parsing of config
  val timers: (Map[String, (TimerRelay, Controller)]) = parseConfig(config) match {
    case Success(timerRelayMap) => timerRelayMap.mapValues(timerRelay=> (timerRelay, setupController(timerRelay)))
    case Failure(e)               => throw e
  }

  def parseConfig(config: Config): Try[Map[String, TimerRelay]] = {
    getConfig(config)                                  //Try[TimerConfig]
      .map(validConfig)                                //Try[Try[TimerConfig]]
      .flatten                                         //Try[TimerConfig]
      .map(_.relays.map(timerRelayConfigToTimerRelay)) //Try[List[TimerRelay]]
      .map(timerRelaysToMap)                           //Try[Map[String,TimerRelay]]
  }

  def update(config: Config) {
    val mapTry = parseConfig(config)
    if (mapTry.isSuccess) {
        mapTry.get.foreach({ case (name, timerRelay) => replacePattern(timerRelay, timers)})
    }
  }

  private[this] def replacePattern(timerRelay: TimerRelay, map: Map[String, (TimerRelay, Controller)]) {
    val (oldTimerRelay, oldController) = map(timerRelay.name)
    oldTimerRelay.calculator.update(timerRelay.pattern)
  }

  private[this] def getConfig(config: Config) : Try[TimerConfig] = {
    implicit val patternHint = new FieldCoproductHint[Pattern]("type") {
      override def fieldValue(name: String) = name.toLowerCase.dropRight("Pattern".length)
    }
    implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
    implicit val localTimeInstancce = localTimeConfigConvert(DateTimeFormatter.ISO_TIME)
    implicit val timeDoubleConverter = new TimeDoubleConverter()
    val timerConfig: Either[ConfigReaderFailures, TimerConfig] = loadConfig[TimerConfig](config.getConfig("timer"))
    timerConfig match {
      case Left(error)        => Failure(new InvalidConfigException("Invalid timer config - " + error))
      case Right(timerConfig) => Success(timerConfig)
    }
  }

  //TODO
  private[this] def validConfig(config: TimerConfig): Try[TimerConfig] = {
    Success(config)
  }

  private[this] def timerRelayConfigToTimerRelay(relayConfig: TimerRelayConfig): TimerRelay = {
    TimerRelay(relayConfig.name,
               relayConfig.pattern,
               new Relay(pigpio, relayConfig.relayId, relayConfig.inverseRelay.getOrElse(false)),
               new TimerCalculation(1, relayConfig.pattern))
  }

  private[this] def timerRelaysToMap(timerRelays: List[TimerRelay]): Map[String, TimerRelay] = {
    val hashmap = HashMap[String, TimerRelay]()
    timerRelays.foldLeft(hashmap)(((map, timerRelay) => map + (timerRelay.name -> timerRelay)))
  }

  /*
   * Setting up the controllers
   */
  private[this] def setupController(timerRelay: TimerRelay): Controller = {
    val controller = new Controller(timerRelay.relay, false)
    controller.addControllee(timerRelay.calculator)
    controller
  }
}
