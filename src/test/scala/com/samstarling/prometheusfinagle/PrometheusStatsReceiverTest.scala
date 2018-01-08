package com.samstarling.prometheusfinagle

import io.prometheus.client.CollectorRegistry

class PrometheusStatsReceiverTest extends UnitTest {

  "PrometheusStatsReceiverTest" should {
    "have a zero-argument constructor" in {
      (new PrometheusStatsReceiver) must not(throwA[RuntimeException])
    }

    "allow a registry to be passed" in {
      val registry = CollectorRegistry.defaultRegistry
      (new PrometheusStatsReceiver(registry)) must not(throwA[RuntimeException])
    }

    "allow a registry and namespace to be passed" in {
      val registry = CollectorRegistry.defaultRegistry
      val namespace = "testnamespace"
      (new PrometheusStatsReceiver(registry, namespace)) must not(throwA[RuntimeException])
    }
  }
}
