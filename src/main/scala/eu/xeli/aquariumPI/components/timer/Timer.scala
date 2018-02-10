package eu.xeli.aquariumPI.components.timer

import eu.xeli.aquariumPI.Controller
import eu.xeli.aquariumPI.gpio.Relay
import eu.xeli.aquariumPI.config.{Relays => RelaysConfigFactory}
import eu.xeli.aquariumPI.config.RelayConfig

import eu.xeli.jpigpio.JPigpio
import java.time.LocalTime
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}
import scala.collection.immutable.HashMap
import com.typesafe.config._

class Timer(pigpio: JPigpio, config: Config, relays: Relays) {
  case class TimerRelay(name: String, pattern: Pattern, relay: Relay, calculator: TimerCalculation)

  //initial parsing of config
  val timers: (Map[String, (TimerRelay, Controller)]) = parseConfig(config) match {
    case Success(timerRelayMap) => timerRelayMap.map({ case (key, timerRelay) => (key, (timerRelay, setupController(timerRelay)))})
    case Failure(e)               => throw e
  }

  def parseConfig(config: Config): Try[Map[String, TimerRelay]] = {
    RelaysConfigFactory.get(config.getConfig("relays")) //Try[RelaysConfig]
      .map(_.relays)                                    //Try[List[RelayConfig]]
      .map(_.filter(_.pattern.isEmpty == false))        //Try[List[RelayConfig]]
      .map(_.map(relayConfigToTimerRelay))              //Try[List[TimerRelay]]
      .map(timerRelaysToMap)                            //Try[Map[String,TimerRelay]]
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

  private[this] def relayConfigToTimerRelay(relayConfig: RelayConfig): TimerRelay = {
    TimerRelay(relayConfig.name,
               relayConfig.pattern.get,
               relays(relayConfig.name).get,
               new TimerCalculation(1, relayConfig.pattern.get))
  }

  private[this] def timerRelaysToMap(timerRelays: List[TimerRelay]): Map[String, TimerRelay] = {
    val hashmap = HashMap[String, TimerRelay]()
    timerRelays.foldLeft(hashmap)((map, timerRelay) => map + (timerRelay.name -> timerRelay))
  }

  /*
   * Setting up the controllers
   */
  private[this] def setupController(timerRelay: TimerRelay): Controller = {
    val controller = new Controller(timerRelay.relay, None)
    controller.addControllee(timerRelay.calculator)
    controller.start()
    controller
  }
}
