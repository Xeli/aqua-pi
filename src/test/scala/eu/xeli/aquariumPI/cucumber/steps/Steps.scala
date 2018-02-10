package eu.xeli.aquariumPI.cucumber.steps

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import cucumber.api.scala.{EN, ScalaDsl}
import eu.xeli.aquariumPI.App
import eu.xeli.jpigpio.{Alert, JPigpio}
import org.scalamock.scalatest.MockFactory

class Steps extends ScalaDsl with EN with MockFactory {
  var config: Config = ConfigFactory.empty()
  var jpigpio: JPigpio = stub[JPigpio]
  var gpioListener: Alert = _

  Given("""^a valid config$""") { () =>
    config = config.withValue("aquaPI.servers.pigpio.host", ConfigValueFactory.fromAnyRef("127.0.0.1"))
    config = config.withValue("aquaPI.servers.pigpio.port", ConfigValueFactory.fromAnyRef("8888"))
    config = config.withValue("aquaPI.servers.aquaStatus.host", ConfigValueFactory.fromAnyRef("192.168.1.1"))
    config = config.withValue("aquaPI.servers.aquaStatus.port", ConfigValueFactory.fromAnyRef("8888"))
    config = config.withValue("aquaPI.gpio.ato.waterlevel", ConfigValueFactory.fromAnyRef(1))
    config = config.withValue("aquaPI.gpio.ato.pump", ConfigValueFactory.fromAnyRef(2))
    config = config.withValue("aquaPI.relays.relays", ConfigValueFactory.fromAnyRef(new java.util.ArrayList[Object]()))
    config = config.withValue("aquaPI.light.channels", ConfigValueFactory.fromIterable(new java.util.ArrayList[Object]()))
  }

  Given("""^the water level sensor is set at (\d) and ato pump at (\d)$""") { (waterLevelSensor: Int, pump: Int) =>
    config = config.withValue("aquaPI.gpio.ato.waterlevel", ConfigValueFactory.fromAnyRef(waterLevelSensor))
    config = config.withValue("aquaPI.gpio.ato.pump", ConfigValueFactory.fromAnyRef(pump))
  }

  When("""^a gpio update is send for pin (\d) with level (\d)$""") {
    (pinInput: Int, level: Int) =>
      gpioListener.alert(pinInput, level, 0L)
  }

  Then("""^pin (\d) is set to (high|low)$""") {
    (pin: Int, levelOutput: String) =>
      val mode = if (levelOutput.equalsIgnoreCase("high")) JPigpio.PI_HIGH else JPigpio.PI_LOW
      (jpigpio.gpioWrite _).verify(pin, mode)

      withExpectations(() -> ())
  }

  When("""^the application starts$""") { () =>
    (jpigpio.gpioSetAlertFunc _).when(*, *).onCall((i, alert) => gpioListener = alert)
    App.startApp(config.getConfig("aquaPI"), jpigpio, Option.empty)
  }

  def getPackagedConfig(): Config = {
    //gets the application.conf from jar resources
    ConfigFactory.load().getConfig("aquaPI")
  }
}