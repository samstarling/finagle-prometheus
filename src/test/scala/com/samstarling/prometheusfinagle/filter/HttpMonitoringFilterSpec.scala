package com.samstarling.prometheusfinagle.filter

import com.samstarling.prometheusfinagle.UnitTest
import com.samstarling.prometheusfinagle.helper.CollectorHelper
import com.samstarling.prometheusfinagle.metrics.Telemetry
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.{Await, Future}
import io.prometheus.client.CollectorRegistry
import org.specs2.specification.Scope

import scala.collection.JavaConverters._

class HttpMonitoringFilterSpec extends UnitTest {

  trait Context extends Scope {
    val registry = new CollectorRegistry(true)
    val telemetry = new Telemetry(registry, "test")
    val labeller = new TestLabeller
    val filter = new HttpMonitoringFilter(telemetry, labeller)
    val service = mock[Service[Request, Response]]
    val request = Request(Method.Get, "/foo/bar")
    val serviceResponse = Response(Status.Created)
    val counter = telemetry.counter(name = "incoming_http_requests_total")
    service.apply(request) returns Future.value(serviceResponse)
  }

  "HttpMonitoringFilter" >> {
    "passes requests on to the next service" in new Context {
      Await.result(filter.apply(request, service))
      there was one(service).apply(request)
    }

    "returns the Response from the next service" in new Context {
      val actualResponse = Await.result(filter.apply(request, service))
      actualResponse ==== serviceResponse
    }

    "increments the incoming_http_requests_total counter" in new Context {
      Await.result(filter.apply(request, service))
      Await.result(filter.apply(request, service))
      CollectorHelper.firstSampleFor(counter).map { sample =>
        sample.value ==== 2.0
      }
    }

    "adds the correct help label" in new Context {
      Await.result(filter.apply(request, service))
      CollectorHelper.firstMetricFor(counter).map { metric =>
        metric.help ==== "The number of incoming HTTP requests"
      }
    }

    "increments the counter with the labels from the labeller" in new Context {
      Await.result(filter.apply(request, service))
      CollectorHelper.firstSampleFor(counter).map { sample =>
        sample.labelNames.asScala(0) ==== "foo"
        sample.labelValues.asScala(0) ==== "bar"
      }
    }
  }
}
