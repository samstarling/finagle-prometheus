package com.samstarling.prometheusfinagle.filter

import com.samstarling.prometheusfinagle.metrics.Telemetry
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.{Future, Stopwatch}

class HttpLatencyMonitoringFilter(telemetry: Telemetry,
                                  buckets: Seq[Double],
                                  labeller: ServiceLabeller =
                                    new HttpServiceLabeller)
    extends SimpleFilter[Request, Response] {

  private val histogram = telemetry.histogram(
    name = "incoming_http_request_latency_seconds",
    help = "A histogram of the response latency for HTTP requests",
    labelNames = labeller.keys,
    buckets = buckets
  )

  override def apply(request: Request,
                     service: Service[Request, Response]): Future[Response] = {
    val stopwatch = Stopwatch.start()
    service(request) onSuccess { response =>
      histogram
        .labels(labeller.labelsFor(request, response): _*)
        .observe(stopwatch().inMilliseconds / 1000.0)
    }
  }
}
