package eu.xeli.aquariumPI

import eu.xeli.aquariumPI.timer.Relays
import eu.xeli.aquariumPI.light.{Light, LightMetric}
import eu.xeli.aquariumPI.avro.{Metric => AvroMetric, Light => AvroLight, Relay => AvroRelay}

import java.time._
import java.util.concurrent._

import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import java.io._

class GatherMetrics(servers: Servers, ato: Option[Ato], temperature: Option[Double], ph: Option[Ph], light: Option[Light], relays: Option[Relays]) extends Runnable {

  def start() {
    val executor = new ScheduledThreadPoolExecutor(1)
    executor.scheduleAtFixedRate(this, 0, 30, TimeUnit.SECONDS)
  }

  def run() {
    val timestamp = Instant.now().getEpochSecond()

    val avroMetric = AvroMetric(
      timestamp,
      ato.map(_.waterLevel),
      None,
      None,
      light.map(gatherLightData),
      relays.map(gatherRelaysData)
    )

    //serialize to byte[]
    val out = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get().binaryEncoder(out, null)
    val writer = new SpecificDatumWriter[AvroMetric](avroMetric.getSchema())

    writer.write(avroMetric, encoder)
    encoder.flush()
    out.close()
    val message = out.toByteArray()
  }

  private[this] def gatherLightData(light: Light): List[AvroLight] = {
    light.channels.map {
      case (name, (_channel, controller)) => AvroLight(name, controller.getCurrentValue)
    }.to[List]
  }

  private[this] def gatherRelaysData(relays: Relays): List[AvroRelay] = {
    relays.map.map {
      case (name, relay) => AvroRelay(name, relay.getValue())
    }.to[List]
  }

}
