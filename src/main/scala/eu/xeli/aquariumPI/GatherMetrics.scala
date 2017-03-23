package eu.xeli.aquariumPI

import eu.xeli.aquariumPI.light.{Light, LightMetric}
import eu.xeli.aquariumPI.kafka.KafkaProperties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.time._

class GatherMetrics(servers: Servers, light: Light, ato: Ato) extends Runnable {

  case class Metrics(unixTimestamp: Int,
                     light: LightMetric,
                     waterLevel: Double)
  val properties = KafkaProperties.producer(servers.kafka)
  val kafka = new KafkaProducer[String, String](properties)

  val kafkaTopic = "aquapi-sensorData"

  def run() {
    val timestamp = Instant.now().getEpochSecond()

    //gather data
    val lightMetric = light.lastValue
    val waterLevel = ato.waterLevel

    //serialize
    val message = timestamp + " => " + lightMetric.blue + "," + lightMetric.white + " - " + waterLevel
    val data = new ProducerRecord[String, String](kafkaTopic, message)
    kafka.send(data)
  }

}
