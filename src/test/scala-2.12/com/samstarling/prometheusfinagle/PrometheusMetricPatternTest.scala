 package com.samstarling.prometheusfinagle

class PrometheusMetricPatternTest extends UnitTest {

  "PrometheusMetricPatternTest" should {
    "match on mutable array" in {
      val receiver = new PrometheusStatsReceiver()
      val input = scala.collection.mutable.ArrayBuffer("client", "testApp_client", "retries", "requeues")
      receiver.metricPattern(input) must not(throwA[MatchError])
    }
  }
}