package eu.xeli.aquariumPI

import eu.xeli.aquariumPI._
import eu.xeli.aquariumPI.light.Light
import eu.xeli.aquariumPI.timer.Timer
import eu.xeli.aquariumPI.timer.Relays
import eu.xeli.aquariumPI.gpio.Pigpio
import eu.xeli.aquariumPI.config.InvalidConfigException
import eu.xeli.aquariumPI.config.{Relays => RelaysConfigFactory}

import eu.xeli.jpigpio.JPigpio
import collection.JavaConverters._
import com.typesafe.config._
import java.util.concurrent._
import java.io.File
import java.nio.file.Paths
import scala.util.{Try, Success, Failure}

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

    try {
      //ato has a listener, so it doesn't need a loop but acts event based
      val waterLevelSensor = conf.getInt("gpio.ato.waterlevel")
      val atoPump = conf.getInt("gpio.ato.pump")
      val ato = new Ato(pigpio, waterLevelSensor, atoPump)

      //adjust light every 30 seconds
      val light = new Light(pigpio, conf)

      val relaysConfigTry = RelaysConfigFactory.get(conf.getConfig("relays"))
      val (relays, timer) = relaysConfigTry match {
        case Failure(e) => throw e
        case Success(relaysConfig) => {
          val relays = Relays(pigpio, relaysConfig)
          val timer = new Timer(pigpio, conf, relays)
          (relays, timer)
        }
      }

      //ph
      val ph = new Ph(servers.pigpio, maybeConfigDir, 0x4D, 1)

      if(!maybeConfigDir.isEmpty) {
        val configFilePath = maybeConfigDir.get
        val lightFileWatcher = new FileWatcher(Paths.get(configFilePath), "light.conf", () => light.update(getConfig(maybeConfigDir)))
        val timerFileWatcher = new FileWatcher(Paths.get(configFilePath), "timer.conf", () => timer.update(getConfig(maybeConfigDir)))
      }


      //send metrics to kafka every 5 seconds
      //val gatherMetrics = new GatherMetrics(servers, ???, ato)
      //val metricExecutor = new ScheduledThreadPoolExecutor(1)
      //executor.scheduleAtFixedRate(gatherMetrics, 0, 5, TimeUnit.SECONDS)

    } catch {
      case e: InvalidConfigException => {
        println(e)
        System.exit(1)
      }
    }
  }

  def getConfig(configDir: Option[String]): Config = {
    //gets the application.conf from jar resources
    val baseConfig = ConfigFactory.load().getConfig("aquaPI")

    val configsFilename = Seq("light.conf", "relays.conf", "servers.conf")
    configDir match {
      case Some(configDirString) => {
        val configsPath = configsFilename.map(configDirString + "/" + _)
        configsPath.foldLeft(baseConfig)(addConfigs)
      }
      case None => baseConfig
    }
  }

  def addConfigs(baseConfig: Config, configPath: String): Config = {
    println("Getting more configs" + configPath)
    val file = new File(configPath)
    val config = ConfigFactory.parseFile(file)

    config.withFallback(baseConfig)
  }

  def getServers(conf: Config): Servers = {
    val kafka = new Server(conf.getString("servers.kafka.host"), conf.getInt("servers.kafka.port"))

    val pigpioHost = conf.getString("servers.pigpio.host")
    val pigpioPort = conf.getInt("servers.pigpio.port")
    val pigpio = new Server(pigpioHost, pigpioPort)
    new Servers(pigpio, kafka)
  }
}
