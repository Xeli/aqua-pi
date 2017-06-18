package eu.xeli.aquariumPI.light

import LightCalculation.LightPattern
import eu.xeli.aquariumPI.gpio.PwmGroup
import eu.xeli.aquariumPI.Controller
import eu.xeli.aquariumPI.config.pureconfig.TimeDoubleConverter

import scala.collection.immutable.HashMap
import scala.util.{Try, Success, Failure}
import java.util.concurrent._
import java.time._

import eu.xeli.jpigpio.JPigpio
import com.typesafe.config._
import pureconfig._
import pureconfig.error.ConfigReaderFailures

/*
 * This class drives the lights by using pwms
 * The lights configuration is retrieved from a config file
 */
class Light(pigpio: JPigpio, config: Config) {
  case class LightChannel(name: String, pattern: LightPattern, pins: PwmGroup, calculator: LightCalculation)


  def updateChannels(newConfig: Config) {
      val lightChannelsTry = parseConfig(newConfig)
      if (lightChannelsTry.isSuccess) {
        lightChannelsTry.get.foreach({ case (name, lightChannel) => replacePattern(lightChannel, channels)})
      }
  }

  def parseConfig(config: Config): Try[Map[String, LightChannel]] = {
    val emptyTryList: Try[List[LightChannel]] = Try(List())
    getLightConfig(config)                          //Try[LightConfig]
      .map(_.channels)                              //Try[List[LightChannelConfig]]
      .map(_.map(lightChannelConfigToLightChannel)) //Try[List[LightChannel]]
      .map(lightChannelsToMap)                      //Try[Map[String, LightChannel]
  }

  /*
   * Converting LightConfig => LightChannels
   */
  case class LightConfig(channels: List[LightChannelConfig])
  case class LightChannelConfig(name: String, pattern: LightPattern, pins: List[Int])

  private[this] def getLightConfig(config: Config): Try[LightConfig] = {
    implicit val timeDoubleConverter = new TimeDoubleConverter()
    val lightConfig: Either[ConfigReaderFailures, LightConfig] = loadConfig[LightConfig](config.getConfig("light"))
    lightConfig match {
      case Left(error)        => Failure(new Exception("Light config load exception"))
      case Right(lightConfig) => Success(lightConfig)
    }
  }

  private[this] def lightChannelConfigToLightChannel(channel: LightChannelConfig): LightChannel = {
    LightChannel(channel.name,
                 channel.pattern,
                 new PwmGroup(pigpio, channel.pins),
                 new LightCalculation(1, channel.pattern))
  }

  private[this] def lightChannelsToMap(lightChannels: List[LightChannel]): Map[String, LightChannel] = {
    val hashmap = HashMap[String, LightChannel]()
    lightChannels.foldLeft(hashmap)(((map, channel) => map + (channel.name -> channel)))
  }

  /*
   * Setting up the controllers
   */
  private[this] def setupController(channel: LightChannel): Controller = {
    val controller = new Controller(maxOffset = 10, secondsToTransition = 1, output = channel.pins)
    controller.addControllee(channel.calculator)
    controller
  }

  /*
   * Updating controllers with the new pattern
   */
  private[this] def replacePattern(newLightChannel: LightChannel, channels: Map[String, (LightChannel, Controller)]) {
    val (channel, controller) = channels(newLightChannel.name)
    channel.calculator.setSections(newLightChannel.pattern)
  }

  val channels:(Map[String, (LightChannel, Controller)]) = parseConfig(config) match {
    case Success(lightChannelMap) => lightChannelMap.mapValues(channel => (channel, setupController(channel)))
    case Failure(e)               => throw e
  }
}
