package eu.xeli.aquariumPI.config

import eu.xeli.aquariumPI.Servers

import com.typesafe.config._
import _root_.pureconfig._
import _root_.pureconfig.configurable._
import _root_.pureconfig.error.ConfigReaderFailures

object ServersConfig {
  def get(config: Config): Servers = {
    //converters & hints
    implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

    //parse the config
    val serversConfig: Either[ConfigReaderFailures, Servers] = loadConfig[Servers](config)
    serversConfig match {
      case Left(error)        => throw new InvalidConfigException("Invalid servers config - " + error)
      case Right(servers) => servers 
    }
  }
}
