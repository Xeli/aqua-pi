package eu.xeli.aquariumPI.light

import LightCalculation.LightPattern
import eu.xeli.aquariumPI.ConfigUtils
import eu.xeli.aquariumPI.gpio._
import eu.xeli.aquariumPI.Controller

import jpigpio.JPigpio
import java.time._
import com.typesafe.config._
import scala.collection.immutable.HashMap
import java.util.concurrent._
import java.io.File
import java.nio.file.Paths

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

  def parseLightChannels(config: Config): Map[String, LightChannel] = {
    val list = ConfigUtils.convertListConfig(config, "light").map(parseLightChannel(_))
    val hashmap = HashMap[String, LightChannel]()
    list.foldLeft(hashmap)((m,lc) => m + (lc.name -> lc))
  }

  def parseLightChannel(config: Config): LightChannel = {
    val name = config.getString("name")

    val pattern = ConfigUtils.convertListStringDouble(config, "pattern")
    val calculator = new LightCalculation(1, pattern)

    val pins = ConfigUtils.convertListInt(config, "pins")
    val pwms = new PwmGroup(pigpio, pins)

    LightChannel(name, pattern, pwms, calculator)
  }

  def setupController(channel: LightChannel): Controller = {
    val controller = new Controller(1, 10, channel.pins)
    controller.addControllee(channel.calculator)
    controller
  }

  def updateChannels(newConfig: Config) {
      val lightChannels = parseLightChannels(newConfig)
      calculators.map(replacePattern(_, lightChannels))
  }

  def replacePattern(entry: (String, LightChannel), newValues: Map[String, LightChannel]) {
    val (name, lightChannel) = entry
    val newLightChannel = newValues(name)
    lightChannel.calculator.setSections(newLightChannel.pattern)
  }

  var lastValue = LightMetric(0,0)
  val calculators: Map[String, LightChannel] = parseLightChannels(config)
  val controllers: Seq[Controller] = calculators.map({case (k:String,v:LightChannel) => setupController(v)}).to[collection.immutable.Seq]
}
