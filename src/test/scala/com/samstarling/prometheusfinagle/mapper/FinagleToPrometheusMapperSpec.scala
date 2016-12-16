package com.samstarling.prometheusfinagle.mapper

import com.samstarling.prometheusfinagle.UnitTest
import com.twitter.common.metrics.Metrics
import org.specs2.specification.Scope

class FinagleToPrometheusMapperSpec extends UnitTest {

  trait Context extends Scope {
    val metrics = Metrics.createDetached
    val mapper = new FinagleToPrometheusMapper(metrics)
  }
}
