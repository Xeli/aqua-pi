package eu.xeli.aquariumPI

import gpio.Listener
import eu.xeli.aquariumPI._
import eu.xeli.aquariumPI.light.{Light, LightCalculation}
import com.typesafe.config._
import java.util.concurrent._

/**
 * @author ${user.name}
 */
object App {

  def main(args : Array[String]) {
    println("Starting up")

    val conf = getConfig()
    val servers = getServers(conf)

    //ato has a listener, so it doesn't need a loop but acts event based
    val ato = setupATO(servers, conf)

    //adjust light every 30 seconds
    val light = setupLight(servers, conf)
    val executor = new ScheduledThreadPoolExecutor(1)
    executor.scheduleAtFixedRate(light, 0, 30, TimeUnit.SECONDS)

    //send metrics to kafka every 5 seconds
    val gatherMetrics = new GatherMetrics(servers, light, ato)
    val metricExecutor = new ScheduledThreadPoolExecutor(1)
    executor.scheduleAtFixedRate(gatherMetrics, 0, 5, TimeUnit.SECONDS)
  }

  def setupATO(servers: Servers, conf: Config): Ato = {
    val waterLevelSensor = conf.getInt("gpio.ato.waterlevel")
    val atoPump = conf.getInt("gpio.ato.pump")
    new Ato(servers, waterLevelSensor, atoPump)
  }

  def setupLight(servers: Servers, conf: Config): Light = {
    //TODO get blues/whites from config file
    val bluesData:List[(String, Double)] = List(("00:30", 0), ("08:30", 0), ("11:00", 80), ("17:30", 50), ("22:00", 3), ("23:00", 3))
    val whitesData:List[(String, Double)] = List(("00:30", 0), ("08:30", 0), ("11:00", 80), ("17:30", 40), ("22:00", 1), ("23:00", 0))

    val blues = new LightCalculation(bluesData)
    val whites = new LightCalculation(whitesData)
    val bluePins = List(22,24)
    val whitePins = List(14,25)
    new Light(servers, bluePins, whitePins, blues, whites)
  }

  def getConfig(): Config = {
    ConfigFactory.load()
  }

  def getServers(conf: Config): Servers = {
    val kafka = new Server(conf.getString("servers.kafka.host"), conf.getInt("servers.kafka.port"))

    val pigpioHost = conf.getString("servers.pigpio.host")
    val pigpioPort = conf.getInt("servers.pigpio.port")
    val pigpio = new Server(pigpioHost, pigpioPort)
    new Servers(pigpio, kafka)
  }

}
