package eu.xeli.aquariumPI

import eu.xeli.aquariumPI.light.{Light, LightMetric}
import eu.xeli.aquariumPI.avro.{Metric => AvroMetric, Light => AvroLight}

import java.time._

import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import java.io._

class GatherMetrics(servers: Servers, light: Light, ato: Ato) extends Runnable {

  case class Metrics(unixTimestamp: Int,
                     light: LightMetric,
                     waterLevel: Double)

  def run() {
    val timestamp = Instant.now().getEpochSecond()

    //gather data and serialize
    val avroLights = light.channels
      .mapValues({ case (lightChannel, controller) => controller.getCurrentValue})
      .map({case (name, value) => AvroLight(name, value)})
      .to[List]
    val waterLevel = ato.waterLevel
    val avroMetric = AvroMetric(timestamp, ato.waterLevel, avroLights)

    //serialize to byte[]
    val out = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get().binaryEncoder(out, null)
    val writer = new SpecificDatumWriter[AvroMetric](avroMetric.getSchema())

    writer.write(avroMetric, encoder)
    encoder.flush()
    out.close()
    val message = out.toByteArray()
  }

}
