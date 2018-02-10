package eu.xeli.aquariumPI.config

import eu.xeli.aquariumPI.components.timer.Pattern
import eu.xeli.aquariumPI.config.pureconfig.TimeDoubleConverter

import java.time.format.DateTimeFormatter
import scala.util.{Try, Success, Failure}
import com.typesafe.config._
import _root_.pureconfig._
import _root_.pureconfig.configurable._
import _root_.pureconfig.error.ConfigReaderFailures

/*
 * Config case-classes
 */
case class RelaysConfig(relays: List[RelayConfig])
case class RelayConfig(name: String, relayId: Int, inverseRelay: Option[Boolean], pattern: Option[Pattern])

object Relays {
  def get(config: Config): Try[RelaysConfig] = {
    //converters & hints
    implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

    implicit val patternHint = new FieldCoproductHint[Pattern]("type") {
      override def fieldValue(name: String) = name.toLowerCase.dropRight("Pattern".length)
    }
    implicit val localTimeInstance = localTimeConfigConvert(DateTimeFormatter.ISO_TIME)
    implicit val timeDoubleConverter = new TimeDoubleConverter()

    //parse the config
    val relaysConfig: Either[ConfigReaderFailures, RelaysConfig] = loadConfig[RelaysConfig](config)
    relaysConfig match {
      case Left(error)        => Failure(new InvalidConfigException("Invalid relays config - " + error))
      case Right(relaysConfig) => Success(relaysConfig)
    }
  }
}
