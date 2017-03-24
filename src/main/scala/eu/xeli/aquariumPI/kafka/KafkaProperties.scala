package eu.xeli.aquariumPI.kafka

import eu.xeli.aquariumPI.Server
import java.util.Properties
import com.typesafe.config._

object KafkaProperties {
  def producer(kafka: Server): Properties = {
    val properties = new Properties()
    properties.put("bootstrap.servers", kafka.host + ":" + kafka.port)
    properties.put("client.id", "AquaPI")
    properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")
    properties
  }
}
