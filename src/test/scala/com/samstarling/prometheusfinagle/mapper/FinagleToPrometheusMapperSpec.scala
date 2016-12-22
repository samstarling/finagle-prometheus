package com.samstarling.prometheusfinagle.mapper

import com.samstarling.prometheusfinagle.UnitTest
import com.twitter.common.metrics.Metrics
import org.specs2.specification.Scope

import scala.collection.JavaConverters._

class FinagleToPrometheusMapperSpec extends UnitTest {

  trait Context extends Scope {
    val metrics = Metrics.createDetached
    val mapper = new FinagleToPrometheusMapper(metrics)
  }

  "#metricFamilySamples" >> {

    "counters" >> {
      trait CounterContext extends Context {
        metrics.registerCounter("number_of_bears")
        metrics.registerCounter("number_of_dogs")
        metrics.registerCounter("number_of_cats")
      }

      "returns a metric for each counter" in new CounterContext {
        mapper.metricFamilySamples must have size(3)
      }
    }
  }
}

