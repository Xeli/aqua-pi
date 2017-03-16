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
  }

  def getConfig(): Config = {
    ConfigFactory.load()
  }

}
