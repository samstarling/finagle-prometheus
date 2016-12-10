package com.samstarling.service

import java.io.StringWriter

import com.samstarling.metrics.Telemetry
import com.twitter.common.metrics.Metrics
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat

import scala.collection.JavaConverters._

class MetricsService(registry: CollectorRegistry, telemetry: Telemetry) extends Service[Request, Response] {

  override def apply(request: Request): Future[Response] = {
    updateFinagleMetrics()
    val writer = new StringWriter
    TextFormat.write004(writer, registry.metricFamilySamples())
    val response = Response(request.version, Status.Ok)
    response.setContentString(writer.toString)
    Future(response)
  }

  private def updateFinagleMetrics(): Unit = {
    Metrics.root.sampleCounters().asScala.foreach { case (name, value) =>
      val c = telemetry.gauge(name.replace("/", "__").replace(".", "_"))
      c.set(value.doubleValue())
    }
  }
}
