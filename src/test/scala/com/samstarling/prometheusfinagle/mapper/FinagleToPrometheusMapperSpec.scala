package com.samstarling.prometheusfinagle.mapper

import com.samstarling.prometheusfinagle.UnitTest
import com.twitter.common.metrics.{Gauge, Histogram, Metrics}
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

    "histograms" >> {
      trait HistogramContext extends Context {
        val histogram = new Histogram("request_latency")
        metrics.registerHistogram(histogram)
      }

      "returns five metrics for a single histogram" in new HistogramContext {
        // Five metrics: quantiles, min, max, count and sum
        mapper.metricFamilySamples must have size(5)
      }
    }

    "gauges" >> {

      class TestGauge extends Gauge[Number] {
        override def getName: String = "water_level"
        override def read(): Number = 123.45
      }

      trait GaugeContext extends Context {
        val gauge: Gauge[Number] = new TestGauge
        metrics.registerGauge(gauge)
      }

      "returns a metric for each gauge" in new GaugeContext {
        mapper.metricFamilySamples must have size(1)
      }
    }
  }
}

