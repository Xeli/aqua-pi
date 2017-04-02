package eu.xeli.aquariumPI

import collection.mutable.PriorityQueue
import java.util.concurrent._

/*
 *
 * This class is to be used when you have multiple 'Controllees'
 * that each might want to control an output. This controller will,
 * based on their relative priority, handle which value is set.
 *
 * Optionally it also 'eases' into this new function to prevent stuttering behaviors
 *
 * Example: You have a light which runs at a percentage.
 * Two controllees want to produce values for this light:
 *    - The first one is a sunset module, each 5 seconds it changes the percentage slightly
 *    - The second is a manual override, usually it doesn't care for the value (deactivated)
 *      but sometimes it wants to force the value to 100% for a couple of minutes.
 * The controller will adjust the light every 5 seconds for the sunset module, say it's at 12% now
 * if at any point the second override module wants to force a value, say 90%, this controller can 'ease':
 *    instead of going from 12% to 90%, it will go from 12% - 38% - 64% - 90% in several seconds.
 *    the actual step size can be given as parameter in the constructor
 */
class Controller(maximumStepSize: Double, output: Output) extends Runnable {
  val controllees = PriorityQueue[Controllee]()

  @volatile
  var currentValue:Double = 0.0

  @volatile
  var updateFrequency = 5

  val executor = new ScheduledThreadPoolExecutor(1)
  @volatile
  var future = executor.scheduleAtFixedRate(this, updateFrequency, updateFrequency, TimeUnit.SECONDS)

  def addControllee(controllee: Controllee) {
    controllees.enqueue(controllee)
    val oldFrequency = updateFrequency
    updateFrequency = Math.min(updateFrequency, controllee.getFrequency())

    if(oldFrequency != updateFrequency) {
      future.cancel(true)
      future = executor.scheduleAtFixedRate(this, 0, updateFrequency, TimeUnit.SECONDS)
    }
  }

  def getCurrentTarget(): Double = {
    controllees.find(_.activated) match {
      case Some(controllee) => controllee.getValue()
      case None => 0.0
    }
  }

  def run() {
    print("Current target: " + getCurrentTarget() + " => ")
    val delta = getCurrentTarget() - currentValue

    //if the change is within the threshold, just change to it
    //otherwise change it with maxmimumStepSize sized steps over time
    setValue(currentValue + Math.min(delta, maximumStepSize))
  }

  def setValue(value: Double, force: Boolean = false) {
    if (currentValue != value || force) {
      output.setValue(value)
      currentValue = value
      println(value)
    }
  }
}
