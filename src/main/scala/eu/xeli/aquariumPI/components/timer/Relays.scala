package eu.xeli.aquariumPI.components.timer

import eu.xeli.aquariumPI.gpio.Relay
import eu.xeli.aquariumPI.config.RelayConfig
import eu.xeli.aquariumPI.config.RelaysConfig

import eu.xeli.jpigpio.JPigpio
import scala.collection.immutable.HashMap

case class Relays(map: Map[String, Relay]) {
  def apply(name: String): Option[Relay] = map.get(name)
}

object Relays {
  def apply(pigpio: JPigpio, config: RelaysConfig): Relays = {
    val hashmap = HashMap[String, Relay]()
    val map = config.relays.foldLeft(hashmap)((map, relayConfig) => map + (relayConfig.name -> getRelay(pigpio, relayConfig)))
    Relays(map)
  }

  private[this] def getRelay(pigpio: JPigpio, config: RelayConfig): Relay = {
    new Relay(pigpio, config.relayId, config.inverseRelay.getOrElse(false))
  }

}
