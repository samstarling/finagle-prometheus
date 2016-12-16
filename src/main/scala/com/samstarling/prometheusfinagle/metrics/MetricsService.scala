package com.samstarling.prometheusfinagle.metrics

import java.io.StringWriter

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import io.prometheus.client.Collector.MetricFamilySamples
import io.prometheus.client.exporter.common.TextFormat

import scala.collection.JavaConverters._

// TODO: Is List[T] the most sensible type here? What about Iterator or Enumeration?
class MetricsService(samples: => List[MetricFamilySamples]) extends Service[Request, Response] {

  override def apply(request: Request): Future[Response] = {
    val writer = new StringWriter
    TextFormat.write004(writer, samples.iterator.asJavaEnumeration)
    val response = Response(request.version, Status.Ok)
    response.setContentString(writer.toString)
    Future(response)
  }
}
