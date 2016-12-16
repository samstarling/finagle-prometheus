package com.samstarling.prometheusfinagle.metrics

import com.samstarling.prometheusfinagle.UnitTest
import com.twitter.finagle.http.{Method, Request}
import com.twitter.util.Await
import io.prometheus.client.CollectorRegistry
import org.specs2.specification.Scope

import scala.collection.JavaConverters._

class MetricsServiceSpec extends UnitTest {

  trait Context extends Scope {
    val registry = new CollectorRegistry(true)
    val telemetry = new Telemetry(registry, "unit_test")
    val service = new MetricsService(registry.metricFamilySamples.asScala.toList)
  }

  "it renders metrics correctly" in new Context {
    telemetry.counter("foo").inc()
    val request = Request(Method.Get, "/")
    val response = Await.result(service.apply(request))

    response.getContentString.trim ====
      "# HELP unit_test_foo No help provided\n" +
      "# TYPE unit_test_foo counter\n" +
      "unit_test_foo 1.0"
  }
}
