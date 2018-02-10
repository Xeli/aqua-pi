package eu.xeli.aquariumPI.components

import java.io.File

import breeze.linalg.{DenseMatrix, DenseVector}
import breeze.stats.regression.leastSquares
import com.typesafe.config._
import eu.xeli.aquariumPI.ConfigUtils
import eu.xeli.aquariumPI.gpio.I2c
import eu.xeli.jpigpio.JPigpio

class Ph(pigpio: JPigpio, maybeConfigPath: Option[String], i2cAddress: Int, i2cBusNumber: Int) {
  type CalibrationPoints = Seq[(Double, Double)]

  val i2c = new I2c(pigpio, i2cAddress, i2cBusNumber)
  val defaultCalibrationPoints = Seq((0.0, 0.0), (1.0, 1.0))

  var calibrationPoints = readFromFile(maybeConfigPath)

  val (intercept, slope) = getInterceptSlope(calibrationPoints)

  def getValue(): Double = intercept + i2c.getValue(2) * slope

  def getInterceptSlope(calibrationPoints: CalibrationPoints): (Double, Double) = {
    //make sure we have enough data points
    if (calibrationPoints.size < 2) {
      return (0, 1)
    }

    //copy the first element if we don't have enough data points to please breeze
    val first = calibrationPoints.head
    val points =
      if(calibrationPoints.size < 4) first +: first +: calibrationPoints
      else calibrationPoints

    //convert to breeze types and run leastsquares
    val (x, y) = points.unzip
    val xDM = DenseMatrix(x.map((1.0, _)):_*)
    val yDV = DenseVector(y:_*)
    val result = leastSquares(xDM, yDV)
    val intercept = result.coefficients.data(0)
    val slope = result.coefficients.data(1)

    (intercept, slope)
  }

  def readFromFile(maybeConfig: Option[String]): CalibrationPoints = {
    if(maybeConfig.isEmpty) return defaultCalibrationPoints

    val configFile = new File(maybeConfig.get + "/ph.conf")
    if (!configFile.exists()) return defaultCalibrationPoints

    val config = ConfigFactory.parseFile(configFile)
    ConfigUtils.convertListDouble(config, "ph.calibration")
  }
}
