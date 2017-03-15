package eu.xeli.aquariumPI

import eu.xeli.aquariumPI._
import com.typesafe.config._

/**
 * @author ${user.name}
 */
object App {

  def foo(x : Array[String]) = x.foldLeft("")((a,b) => a + b)

  def main(args : Array[String]) {
    println("Starting up")

    val conf = getConfig()

    //Ato setup
    val waterLevelSensor = conf.getInt("gpio.ato.waterlevel")
    val atoPump = conf.getInt("gpio.ato.pump")
    val ato = new Ato(waterLevelSensor, atoPump)

    ato()
  }

  def getConfig(): Config = {
    ConfigFactory.load()
  }

}
