package eu.xeli.aquariumPI

import gpio.{Relay, Listener}

import java.time.{Duration}
import eu.xeli.jpigpio.JPigpio

class Ato(pigpio: JPigpio, waterLevelSensorPin: Int, pumpPin: Int) extends Component {
  @volatile
  var waterLevel = 0.0
  var running = false

  val pump = new Relay(pigpio, pumpPin, true)
  val listener = new Listener(pigpio, waterLevelSensorPin, Ato.Steady, handleChange)

  def start(): Unit = {
    running = true
  }

  def stop(): Unit = {
    running = false
    pump.disable()
  }

  def handleChange = (level: Double) => {
    if (running) {
      pump.disable()
    } else {
      processWaterLevel(level)
    }
  }

  def processWaterLevel(level: Double): Unit = {
    waterLevel = level
    if (level > 0) {
      pump.enable()
    } else {
      pump.disable()
    }
  }
}

object Ato {
  val Steady = Duration.ofNanos(3e+8.toInt)
}
