package eu.xeli.aquariumPI

import eu.xeli.aquariumPI._
import eu.xeli.aquariumPI.light.Light
import eu.xeli.aquariumPI.gpio.Pigpio

import jpigpio.JPigpio
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
    val pigpio = Pigpio.getInstance(servers.pigpio)

    //ato has a listener, so it doesn't need a loop but acts event based
    val ato = setupATO(pigpio, conf)

    //adjust light every 30 seconds
    val light = new Light(pigpio, conf)

    if(!maybeConfigDir.isEmpty) {
      val configFilePath = maybeConfigDir.get
      val fileWatcher = new FileWatcher(Paths.get(configFilePath), "light.conf", () => light.updateChannels(getConfig(maybeConfigDir)))
    }

    //ph
    val ph = new Ph(servers.pigpio, maybeConfigDir, 0x4D, 1)

    //send metrics to kafka every 5 seconds
    //val gatherMetrics = new GatherMetrics(servers, ???, ato)
    //val metricExecutor = new ScheduledThreadPoolExecutor(1)
    //executor.scheduleAtFixedRate(gatherMetrics, 0, 5, TimeUnit.SECONDS)
  }

  def setupATO(pigpio: JPigpio, conf: Config): Ato = {
    val waterLevelSensor = conf.getInt("gpio.ato.waterlevel")
    val atoPump = conf.getInt("gpio.ato.pump")
    new Ato(pigpio, waterLevelSensor, atoPump)
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
