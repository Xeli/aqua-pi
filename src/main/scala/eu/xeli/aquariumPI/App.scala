package eu.xeli.aquariumPI

import gpio.{Server, Listener}
import eu.xeli.aquariumPI._
import com.typesafe.config._

/**
 * @author ${user.name}
 */
object App {

  def main(args : Array[String]) {
    println("Starting up")

    val conf = getConfig()
    val server = new Server(conf.getString("server.host"), conf.getInt("server.port"))

    //Ato setup
    val waterLevelSensor = conf.getInt("gpio.ato.waterlevel")
    val atoPump = conf.getInt("gpio.ato.pump")
    val ato = new Ato(server, waterLevelSensor, atoPump)

    //TODO get from config file
    val bluesData:List[(String, Double)] = List(("09:00", 1), ("12:00", 50), ("18:00", 80), ("21:00", 30), ("23:59", 1))
    val whitesData:List[(String, Double)] = List(("09:00", 1), ("12:00", 50), ("18:00", 80), ("21:00", 30), ("23:59", 1))
    val blues = new LightCalculation(bluesData)
    val whites = new LightCalculation(whitesData)
    val light = new Light(blues, whites)
  }

  def getConfig(): Config = {
    ConfigFactory.load()
  }

}
