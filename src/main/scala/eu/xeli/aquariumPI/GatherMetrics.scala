package eu.xeli.aquariumPI

import eu.xeli.aquariumPI.light.{Light, LightMetric}
import eu.xeli.aquariumPI.kafka.KafkaProperties
import eu.xeli.aquariumPI.avro.{metric => AvroMetric, light => AvroLight}

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.time._

import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import java.io._

class GatherMetrics(servers: Servers, light: Light, ato: Ato) extends Runnable {

  case class Metrics(unixTimestamp: Int,
                     light: LightMetric,
                     waterLevel: Double)
  val properties = KafkaProperties.producer(servers.kafka)
  val kafka = new KafkaProducer[String, Array[Byte]](properties)

  val kafkaTopic = "aquapi-sensorData"

  def run() {
    val timestamp = Instant.now().getEpochSecond()

    //gather data and serialize
    val avroLight = AvroLight(light.lastValue.blue, light.lastValue.white)
    val waterLevel = ato.waterLevel
    val avroMetric = AvroMetric(timestamp, ato.waterLevel, avroLight)

    //serialize to byte[]
    val out = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get().binaryEncoder(out, null)
    val writer = new SpecificDatumWriter[AvroMetric](avroMetric.getSchema())

    writer.write(avroMetric, encoder)
    encoder.flush()
    out.close()
    val message = out.toByteArray()

    //send the data to kafka
    val data = new ProducerRecord[String, Array[Byte]](kafkaTopic, message)
    kafka.send(data)
  }

}
