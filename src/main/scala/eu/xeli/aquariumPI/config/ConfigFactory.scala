package eu.xeli.aquariumPI.config

import java.io.File

import com.typesafe.config.{Config, ConfigFactory => TypeSafeConfigFactory}

object ConfigFactory {

  def getConfig(configDir: Option[String]): Config = {
    //gets the application.conf from jar resources
    val baseConfig = TypeSafeConfigFactory.load().getConfig("aquaPI")

    val configsFilename = Seq("light.conf", "relays.conf", "servers.conf")
    configDir match {
      case Some(configDirString) => {
        val configsPath = configsFilename.map(configDirString + "/" + _)
        configsPath.foldLeft(baseConfig)(addConfigs)
      }
      case None => baseConfig
    }
  }

  def addConfigs(baseConfig: Config, configPath: String): Config = {
    println("Getting more configs" + configPath)
    val file = new File(configPath)
    val config = TypeSafeConfigFactory.parseFile(file)

    config.withFallback(baseConfig)
  }

}
