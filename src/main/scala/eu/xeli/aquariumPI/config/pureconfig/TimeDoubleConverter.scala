package eu.xeli.aquariumPI.config.pureconfig

import java.time._
import java.time.format._
import com.typesafe.config._
import pureconfig.ConfigReader
import pureconfig.error._
import scala.collection.JavaConverters._

class TimeDoubleConverter extends ConfigReader[(LocalTime, Double)] {

  def configReaderFailures(value: String, toType: String, because: String) =
    ConfigReaderFailures(CannotConvert(value, toType, because, None, ""))

  def from(config: ConfigValue): Either[ConfigReaderFailures, (LocalTime, Double)] = {
    config match {
      case config: ConfigList => {
        val seq:scala.collection.immutable.Seq[Object] = config.unwrapped().asScala.toList
        convertSeqToTuple(seq)
      }
      case _ => Left(configReaderFailures(config.render(), "(LocalTime, Double)","Not a ConfigList"))
    }
  }

  def convertSeqToTuple(seq: Seq[Object]): Either[ConfigReaderFailures, (LocalTime, Double)] = {
    seq match {
      case Seq(time: String, value: Number) => {
        try {
          Right((LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME), value.doubleValue))
        } catch {
          case e: NumberFormatException =>
            Left(configReaderFailures(value.toString, "Double", "Not a double"))
          case e: DateTimeParseException =>
            Left(configReaderFailures(time, "LocalTime", "Not a LocalTime"))
        }
      }
      case _ =>
        Left(configReaderFailures(seq.foldLeft("")(_ + _) , "Buffer[String]", "Not a list of strings"))
    }
  }
}
