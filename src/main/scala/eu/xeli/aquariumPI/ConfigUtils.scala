package eu.xeli.aquariumPI

import com.typesafe.config._
import collection.JavaConverters._

object ConfigUtils {

  def convertToString(item: Any): String = {
    item match {
      case s:String => s
      case _        => throw new Exception()
    }
  }

  def convertToInt(item: Any): Int = {
    item match {
      case i: Int => i
      case _      => throw new Exception()
    }
  }

  def convertToConfig(item: Any): Config = {
    print(item.getClass)
    item match {
      case (c: Config) => c
      case _           => throw new Exception()
    }
  }

  def convertToDouble(item: Any): Double = {
    item match {
      case (i:Double) => i
      case (i:Int)    => i.toDouble
      case _         => throw new Exception()
    }
  }

  def convertToList(item: Object): Seq[_] = {
    item match {
      case item:java.util.List[_] => item.asScala
      case _ => throw new Exception()
    }
  }

  def convertToDoubles(item: Seq[_]): (Double, Double) = {
    val Seq(first, second) = item
    (convertToDouble(first), convertToDouble(second))
  }

  def convertToStringDouble(item: Seq[_]): (String, Double) = {
    val Seq(first, second) = item
    (convertToString(first), convertToDouble(second))
  }


  def convertListDouble(config: Config, key: String): Seq[(Double, Double)] = {
    convertList(config, key)
      .map(convertToList)
      .map(convertToDoubles)
  }

  def convertListStringDouble(config: Config, key: String): Seq[(String, Double)] = {
    convertList(config, key)
      .map(convertToList)
      .map(convertToStringDouble)
  }

  def convertListInt(config: Config, key: String): Seq[Int] =
    convertList(config,key).map(convertToInt)

  def convertList(config: Config, key: String): Seq[Object] = config.getList(key).unwrapped().asScala

  def convertListConfig(config: Config, key: String): Seq[Config] =
    convertList(config, key).map(convertToConfig)
}
