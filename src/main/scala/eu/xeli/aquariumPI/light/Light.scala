package eu.xeli.aquariumPI.light

import LightCalculation.LightPattern
import eu.xeli.aquariumPI.ConfigUtils
import eu.xeli.aquariumPI.gpio._
import eu.xeli.aquariumPI.Controller

import eu.xeli.jpigpio.JPigpio
import java.time._
import com.typesafe.config._
import scala.collection.immutable.HashMap
import scala.util.{Try, Success, Failure}
import java.util.concurrent._

import pureconfig._
import pureconfig.error.ConfigReaderFailures

/*
 *
 * This class drives the lights
 *
 * Lights consist of 2 units with each 2 channels (white/blue)
 *
 * blue will be used as moonlight
 *
 */
class Light(pigpio: JPigpio, config: Config) {
  case class LightChannel(name: String, pattern: LightPattern, pins: PwmGroup, calculator: LightCalculation)

  case class LightConfig(channels: List[LightChannelConfig])
  case class LightChannelConfig(name: String, pattern: List[List[String]], pins: List[Int])

  private def getLightConfig(config: Config): Try[LightConfig] = {
    val lightConfig: Either[ConfigReaderFailures, LightConfig] = loadConfig[LightConfig](config.getConfig("light"))
    lightConfig match {
      case Left(error)        => Failure(new Exception("Light config load exception"))
      case Right(lightConfig) => Success(lightConfig)
    }
  }

  private def lightChannelConfigToLightChannel(channel: LightChannelConfig): Try[LightChannel] = {
    val baseValue:Try[List[(String, Double)]] = Try(List())
    val lightPattern: Try[List[(String, Double)]] = channel.pattern.map(stringListToPatternElement)
                                                                   .foldLeft(baseValue)(foldToTryList)
    lightPattern.map(pattern =>
        LightChannel(channel.name,
                     pattern,
                     new PwmGroup(pigpio, channel.pins),
                     new LightCalculation(1, pattern))
    )
  }

  private def stringListToPatternElement(element: List[String]): Try[(String, Double)] = {
    element match {
      case List(name, value) => {
        val newValue = Try(value.toDouble)
        newValue.map((name, _))
      }
      case _ => Failure(new Exception("Light pattern - wrong list size"))
    }
  }

  private def lightChannelsToMap(lightChannels: List[LightChannel]): Map[String, LightChannel] = {
    val hashmap = HashMap[String, LightChannel]()
    lightChannels.foldLeft(hashmap)(((map, channel) => map + (channel.name -> channel)))
  }

  //convert List[Try[T]] to Try[List[T]]
  //if any element in the list is a failure, the whole function will return a failure
  private def foldToTryList[A](listTry: Try[List[A]], elementTry: Try[A]): Try[List[A]] = {
      elementTry.flatMap(element => listTry.map(list => list :+ element))
  }

  private def setupController(channel: LightChannel): Controller = {
    val controller = new Controller(maxOffset = 10, secondsToTransition = 1, output = channel.pins)
    controller.addControllee(channel.calculator)
    controller
  }

  def updateChannels(newConfig: Config) {
      val lightChannelsTry = parseConfig(newConfig)
      if (lightChannelsTry.isSuccess) {
        lightChannelsTry.get.foreach({ case (name, lightChannel) => replacePattern(lightChannel, channels)})
      }
  }

  private def replacePattern(newLightChannel: LightChannel, channels: Map[String, (LightChannel, Controller)]) {
    val (channel, controller) = channels(newLightChannel.name)
    channel.calculator.setSections(newLightChannel.pattern)
  }

  private def parseConfig(config: Config): Try[Map[String, LightChannel]] = {
    val emptyTryList: Try[List[LightChannel]] = Try(List())
    getLightConfig(config)                          //Try[LightConfig]
      .map(_.channels)                              //Try[List[LightChannelConfig]]
      .map(_.map(lightChannelConfigToLightChannel)) //Try[List[Try[LightChannel]]]
      .map(_.foldLeft(emptyTryList)(foldToTryList)) //Try[Try[List[LightChannel]]
      .flatten                                      //Try[List[LightChannel]]
      .map(lightChannelsToMap)                      //Try[Map[String, LightChannel]
  }

  val channels:(Map[String, (LightChannel, Controller)]) = parseConfig(config) match {
    case Success(lightChannelMap) => lightChannelMap.mapValues(channel => (channel, setupController(channel)))
    case Failure(e)               => throw e
  }
}
