package eu.xeli.aquariumPI

import java.nio.file.Paths

import com.typesafe.config.Config
import eu.xeli.aquariumPI.components.{Ato, Component, Ph}
import eu.xeli.aquariumPI.config.{ConfigFactory, InvalidConfigException, ServersConfig, Relays => RelaysConfigFactory}
import eu.xeli.aquariumPI.gpio.{Pigpio => PigpioFactory}
import eu.xeli.aquariumPI.components.light.Light
import eu.xeli.aquariumPI.components.timer.{Relays, Timer}
import eu.xeli.jpigpio.JPigpio

import scala.util.{Failure, Success}

object App {

  def main(args: Array[String]) {
    println("Starting up")
    val maybeConfigDir = args.headOption

    val config = ConfigFactory.getConfig(maybeConfigDir)
    val servers = ServersConfig.get(config)
    val pigpio = PigpioFactory.getInstance(servers.pigpio)

    startApp(config, pigpio, maybeConfigDir)
  }

  def startApp(config: Config, pigpio: JPigpio, maybeConfigDir: Option[String]): Unit = {
    val app = new App(config, pigpio, maybeConfigDir)
    app.start()
  }
}

class App(config: Config, pigpio: JPigpio, maybeConfigDir: Option[String]) {
  private val servers = ServersConfig.get(config)
  private val relays = getRelays(config.getConfig("relays"))

  private val ato = getAto()
  private val light = new Light(pigpio, config)
  private val timer = getTimer(relays)
  private val ph = getPh()

  if(maybeConfigDir.isDefined) {
    val configFilePath = maybeConfigDir.get
    val lightFileWatcher = new FileWatcher(Paths.get(configFilePath), "light.conf", () => light.update(ConfigFactory.getConfig(maybeConfigDir)))
    val timerFileWatcher = new FileWatcher(Paths.get(configFilePath), "timer.conf", () => timer.update(ConfigFactory.getConfig(maybeConfigDir)))
  }

  //send metrics to kafka every 5 seconds
  //val gatherMetrics = new GatherMetrics(servers, ???, ato)
  //val metricExecutor = new ScheduledThreadPoolExecutor(1)
  //executor.scheduleAtFixedRate(gatherMetrics, 0, 5, TimeUnit.SECONDS)

  def start() = {
    ato.start()
  }

  def stop() = {
    ato.stop()
  }

  private[this] def getAto(): Ato = {
    val waterLevelSensor = config.getInt("gpio.ato.waterlevel")
    val atoPump = config.getInt("gpio.ato.pump")

    //ato has a listener, so it doesn't need a loop but acts event based
    new Ato(pigpio, waterLevelSensor, atoPump)
  }

  private[this] def getTimer(relays: Relays) = {
    new Timer(pigpio, config, relays)
  }

  private[this] def getRelays(relayConfig: Config): Relays = {
    val relaysConfigTry = RelaysConfigFactory.get(relayConfig)

    relaysConfigTry match {
      case Failure(e) => throw e
      case Success(relaysConfig) => {
        Relays(pigpio, relaysConfig)
      }
    }
  }

  private[this] def getPh(): Ph = {
    new Ph(pigpio, maybeConfigDir, 0x4D, 1)
  }
}