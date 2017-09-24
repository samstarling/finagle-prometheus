package com.samstarling.prometheusfinagle.filter

import com.samstarling.prometheusfinagle.metrics.Telemetry
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class HttpMonitoringFilter(telemetry: Telemetry,
                           labeller: HttpServiceLabeller = new HttpServiceLabeller)
  extends SimpleFilter[Request, Response] {

  private val counter = telemetry.counter(
    name = "incoming_http_requests_total",
    help = "The number of incoming HTTP requests",
    labelNames = labeller.keys
  )

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    service(request) onSuccess { response =>
      counter.labels(labeller.labelsFor(request, response): _*).inc()
    }
  }
}
