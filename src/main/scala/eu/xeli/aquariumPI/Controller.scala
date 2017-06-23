package eu.xeli.aquariumPI

import collection.mutable.PriorityQueue
import java.util.concurrent._
import java.time._

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
 *    the actual step size can be given as parameter in the constructor, maxOffset.
 *    This max offset determines how much the value can vary between steps
 */
class Controller(output: Output, easerDuration: Option[Duration]) extends Runnable {
  private[this] val controllees = PriorityQueue[Controllee]()

  @volatile
  private[this] var currentValue:Double = output.getValue()

  private[this] val executor = new ScheduledThreadPoolExecutor(1)

  @volatile
  private[this] var future: ScheduledFuture[_] = null

  private[this] var easer: Easer       = null
  private[this] var startTime: Long    = 0
  private[this] var startValue: Double = 0
  private[this] var endValue: Double   = 0

  def run() {
    if (easerDuration.isEmpty) {
      setValue(getCurrentTarget())
    } else {
      runEaser(easerDuration.get)
    }
  }

  def addControllee(controllee: Controllee) {
    controllees.enqueue(controllee)
    resetUpdateFrequency()
  }

  def getCurrentTarget(): Double = {
    getFirstActivatedControllee match {
      case Some(controllee) => controllee.getValue()
      case None => 0.0
    }
  }

  def getCurrentValue: Double = currentValue

  def start() {
    resetUpdateFrequency()
  }

  private[this] def changeUpdateFrequency(period: Int, unit: TimeUnit) {
      if (future != null) {
        future.cancel(true)
      }
      future = executor.scheduleAtFixedRate(this, 0, period, unit)
  }

  private[this] def resetUpdateFrequency() {
    if (controllees.length == 0) {
      return
    }
    val smallestFrequency = controllees.map(_.getFrequency()).min
    changeUpdateFrequency(smallestFrequency, TimeUnit.SECONDS)
  }

  private[this] def getFirstActivatedControllee(): Option[Controllee] = {
    val controlleesClone = controllees.clone
    var best:Option[Controllee] = None
    while(!controlleesClone.isEmpty && best == None) {
      val controllee = controlleesClone.dequeue
      if (controllee.activated) {
        best = Some(controllee)
      }
    }
    best
  }

  private[this] def runEaser(easerDuration: Duration) {
    val valueEqual = Math.abs(currentValue - getCurrentTarget()) < MagicValues.DOUBLE_TOLERANCE
    if (easer == null && valueEqual) {
      //no easer active and we don't need any easing
      return
    }
    if (easer == null) {
      easer = Easer(easerDuration, LocalTime.now, currentValue, getCurrentTarget())
      changeUpdateFrequency(200, TimeUnit.MILLISECONDS)
    }

    if (easer.isFinished(currentValue)) {
      resetUpdateFrequency()
      easer = null
    } else {
      val easerValue = easer.getValue(LocalTime.now)
      setValue(easerValue)
    }
  }

  private[this] def setValue(value: Double, force: Boolean = false) {
    //equal for doubles
    val valueNotEqual = Math.abs(currentValue - value) > MagicValues.DOUBLE_TOLERANCE
    if (valueNotEqual || force) {
      output.setValue(value)
      currentValue = value
    }
  }
}
