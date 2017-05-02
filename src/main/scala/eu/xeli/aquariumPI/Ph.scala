package eu.xeli.aquariumPI

import gpio.I2c
import breeze.linalg.{DenseMatrix, DenseVector}
import breeze.stats.regression.leastSquares
import collection.JavaConverters._
import com.typesafe.config._
import java.io.File

class Ph(server: Server, maybeConfigPath: Option[String], i2cAddress: Int, i2cBusNumber: Int) {
  type CalibrationPoints = Seq[(Double, Double)]

  val i2c = new I2c(server, i2cAddress, i2cBusNumber)
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
    val convertToScalaList:(Object => Seq[_]) = (item: Object) => {
      item match {
        case item:java.util.List[_] => item.asScala
        case _ => throw new Exception()
      }
    }
    val convertToCalibration:(Seq[_] => (Double, Double)) = (element: Seq[_]) => {
      element match {
        case Seq(ph: Double, measurement: Double) => (ph, measurement)
        case Seq(ph: Int, measurement: Double) => (ph.toDouble, measurement)
        case Seq(ph: Double, measurement: Int) => (ph, measurement.toDouble)
        case Seq(ph: Int, measurement: Int) => (ph.toDouble, measurement.toDouble)
        case _ => throw new Exception()
      }
    }

    config.getList("ph.calibration").unwrapped()
      .asScala
      .map(convertToScalaList)
      .map(convertToCalibration)
  }
}
