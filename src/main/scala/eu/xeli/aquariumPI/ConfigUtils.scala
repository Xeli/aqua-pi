package eu.xeli.aquariumPI

import com.typesafe.config._
import collection.JavaConverters._

object ConfigUtils {


  def convertToScalaList(item: Object): Seq[_] = {
    item match {
      case item:java.util.List[_] => item.asScala
      case _ => throw new Exception()
    }
  }

  def convertToDouble(item: Any): Double = {
    item match {
      case (i:Double) => i
      case (i:Int)    => i.toDouble
      case _         => throw new Exception()
    }
  }

  def convertToDoubles(item: Seq[_]): (Double, Double) = {
    val Seq(first, second) = item
    (convertToDouble(first), convertToDouble(second))
  }

  def convertToStringDoubles(item: Seq[_]): (String, Double) = {
    val Seq(first:String, second) = item
    (first, convertToDouble(second))
  }

  def convertListDouble(config: Config, key: String): Seq[(Double, Double)] = {

    config.getList(key).unwrapped()
      .asScala
      .map(convertToScalaList)
      .map(convertToDoubles)
  }

  def convertListStringDouble(config: Config, key: String): Seq[(String, Double)] = {
    config.getList(key).unwrapped()
      .asScala
      .map(convertToScalaList)
      .map(convertToStringDoubles)
  }
}
