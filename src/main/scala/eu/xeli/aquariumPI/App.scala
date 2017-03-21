package eu.xeli.aquariumPI

import gpio.{Server, Listener}
import eu.xeli.aquariumPI._
import com.typesafe.config._
import java.util.concurrent._

/**
 * @author ${user.name}
 */
object App {

  def main(args : Array[String]) {
    println("Starting up")

    val conf = getConfig()
    val server = new Server(conf.getString("server.host"), conf.getInt("server.port"))

    //ato has a listener, so it doesn't need a loop but acts event based
    val ato = setupATO(server, conf)
    val light = setupLight(server, conf)

    val executor = new ScheduledThreadPoolExecutor(1)
    executor.scheduleAtFixedRate(light, 0, 30, TimeUnit.SECONDS)
  }

  def setupATO(server: Server, conf: Config): Ato = {
    val waterLevelSensor = conf.getInt("gpio.ato.waterlevel")
    val atoPump = conf.getInt("gpio.ato.pump")
    new Ato(server, waterLevelSensor, atoPump)
  }

  def setupLight(server: Server, conf: Config): Light = {
    //TODO get blues/whites from config file
    val bluesData:List[(String, Double)] = List(("00:30", 0), ("08:30", 0), ("11:00", 80), ("17:30", 50), ("22:00", 3), ("23:00", 3))
    val whitesData:List[(String, Double)] = List(("00:30", 0), ("08:30", 0), ("11:00", 80), ("17:30", 40), ("22:00", 1), ("23:00", 0))

    val blues = new LightCalculation(bluesData)
    val whites = new LightCalculation(whitesData)
    val bluePins = List(22,24)
    val whitePins = List(14,25)
    new Light(server, bluePins, whitePins, blues, whites)
  }

  def getConfig(): Config = {
    ConfigFactory.load()
  }

}
