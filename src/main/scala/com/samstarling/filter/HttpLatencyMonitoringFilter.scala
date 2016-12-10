package com.samstarling.filter

import com.samstarling.metrics.Telemetry
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.{Future, Stopwatch}

class HttpLatencyMonitoringFilter(telemetry: Telemetry, buckets: Seq[Double]) extends SimpleFilter[Request, Response] {

  private val histogram = telemetry.histogram(
    name = "incoming_http_request_latency_seconds",
    help = "A histogram of the response latency for HTTP requests",
    labelNames = Seq("method", "status", "statusClass"),
    buckets = buckets
  )

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val stopwatch = Stopwatch.start()
    service(request) onSuccess { response =>
      histogram.labels(
        request.method.toString,
        response.status.code.toString,
        StatusClass.forStatus(response.status)
      ).observe(stopwatch().inMilliseconds / 1000.0)
    }
  }
}
