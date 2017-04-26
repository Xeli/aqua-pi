package eu.xeli.aquariumPI

import gpio.{Listener, PwmGroup}
import eu.xeli.aquariumPI._
import eu.xeli.aquariumPI.light.LightCalculation
import eu.xeli.aquariumPI.light.LightCalculation._

import collection.JavaConverters._
import com.typesafe.config._
import java.util.concurrent._
import java.io.File
import java.nio.file.Paths

/**
 * @author ${user.name}
 */
object App {

  def main(args : Array[String]) {
    println("Starting up")
    val maybeConfigDir = args.headOption

    val conf = getConfig(maybeConfigDir)
    val servers = getServers(conf)

    //ato has a listener, so it doesn't need a loop but acts event based
    val ato = setupATO(servers, conf)

    //adjust light every 30 seconds
    val (blues, whites) = setupLight(servers, conf, maybeConfigDir)
    //val light = setupLight(servers, conf)
    val executor = new ScheduledThreadPoolExecutor(1)
    //executor.scheduleAtFixedRate(light, 0, 30, TimeUnit.SECONDS)

    //send metrics to kafka every 5 seconds
    //val gatherMetrics = new GatherMetrics(servers, ???, ato)
    //val metricExecutor = new ScheduledThreadPoolExecutor(1)
    //executor.scheduleAtFixedRate(gatherMetrics, 0, 5, TimeUnit.SECONDS)
  }

  def setupATO(servers: Servers, conf: Config): Ato = {
    val waterLevelSensor = conf.getInt("gpio.ato.waterlevel")
    val atoPump = conf.getInt("gpio.ato.pump")
    new Ato(servers, waterLevelSensor, atoPump)
  }

  def setupLight(servers: Servers, conf: Config, maybeConfigDir: Option[String]): (Controller, Controller) = {
    //TODO get blues/whites from config file
    val bluesData = parseLightData(conf, "light.channels.blue")
    val whitesData = parseLightData(conf, "light.channels.white")

    val blues = setupLightChannel(servers, List(22,24), bluesData)
    val whites = setupLightChannel(servers, List(14,25), whitesData)

    if(!maybeConfigDir.isEmpty) {

      val update = (() => println("update"))

      val configFilePath = maybeConfigDir.get
      val fileWatcher = new FileWatcher(Paths.get(configFilePath), "light.conf", update)
    }
    //val overrideBlue = new OverrideControllee(10, "light-blue")
    //val executor = new ScheduledThreadPoolExecutor(1)
    //executor.scheduleAtFixedRate(overrideBlue, 5, 30, TimeUnit.SECONDS)
    //blues.addControllee(overrideBlue)
    (blues, whites)
  }

  def parseLightData(conf: Config, key: String): LightPattern = {
    val rawList = conf.getList(key).unwrapped().asScala
    rawList.map(a => ("12:00", 0.0))
  }

  def setupLightChannel(servers: Servers, pins: List[Int], pattern: LightPattern): Controller = {
    val sunset = new LightCalculation(1, pattern)
    val pwms = new PwmGroup(servers.pigpio, pins)

    val controller = new Controller(1, 10, pwms)
    controller.addControllee(sunset)
    controller
  }

  def getConfig(configDir: Option[String]): Config = {
    //gets the application.conf from jar resources
    val baseConfig = ConfigFactory.load().getConfig("aquaPI")

    configDir match {
      case Some(configDirString) => addConfigs(baseConfig, configDirString)
      case None => baseConfig
    }
  }

  def addConfigs(baseConfig: Config, dir: String): Config = {
    println("Getting more configs")
    val light = new File(dir + "/light.conf")
    val lightConfig = ConfigFactory.parseFile(light)

    val server = new File(dir + "/servers.conf")
    val serverConfig = ConfigFactory.parseFile(server)

    val configs = List(lightConfig, serverConfig)

    configs.fold(baseConfig){ (config, newConfig) => newConfig.withFallback(config) }
  }

  def getServers(conf: Config): Servers = {
    val kafka = new Server(conf.getString("servers.kafka.host"), conf.getInt("servers.kafka.port"))

    val pigpioHost = conf.getString("servers.pigpio.host")
    val pigpioPort = conf.getInt("servers.pigpio.port")
    val pigpio = new Server(pigpioHost, pigpioPort)
    new Servers(pigpio, kafka)
  }

}
