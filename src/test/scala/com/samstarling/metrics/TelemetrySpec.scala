package com.samstarling.metrics

import com.samstarling.UnitTest
import com.samstarling.helper.CollectorHelper
import io.prometheus.client.CollectorRegistry
import org.specs2.specification.Scope
import scala.collection.JavaConverters._

import scala.collection.mutable

class TelemetrySpec extends UnitTest {

  trait Context extends Scope {
    val registry = new CollectorRegistry(true)
    val telemetry = new Telemetry(registry, "test")
  }

  /**
    * It's not possible to fetch the name, help or labels from Counter/Histogram/Gauge objects themselves, so we
    * have to perform an action against them (such as incrementing a counter) and inspect the resulting metrics
    * and/or samples to ensure that everything is as we expect it.
    */

  "#counter" >> {

    trait CounterContext extends Context {
      val name = "widgets_total"
      val help = "Number of widgets"
      val labelNames = Seq("type", "colour")
      val counter = telemetry.counter(name, help, labelNames)
      counter.labels("sprocket", "red").inc(1)
    }

    "does not try to register duplicates" in new CounterContext {
      // The CollectorRegistry will not allow duplicates to be registered.
      val secondCounter = telemetry.counter(name, help, labelNames)
    }

    "returns a counter with" >> {
      "the correct name" in new CounterContext {
        CollectorHelper.firstMetricFor(counter).map { metric =>
          metric.name ==== "test_widgets_total"
        }
      }

      "the correct help label" in new CounterContext {
        CollectorHelper.firstMetricFor(counter).map { metric =>
          metric.help ==== "Number of widgets"
        }
      }

      "the correct label names" in new CounterContext {
        CollectorHelper.firstSampleFor(counter).map { sample =>
          sample.labelNames.asScala ==== mutable.Buffer("type", "colour")
        }
      }

      "the correct label values" in new CounterContext {
        CollectorHelper.firstSampleFor(counter).map { sample =>
          sample.labelValues.asScala ==== mutable.Buffer("sprocket", "red")
        }
      }
    }
  }

  "#gauge" >> {

    trait GaugeContext extends Context {
      val name = "current_population"
      val help = "Current population of a country"
      val labelNames = Seq("country", "continent")
      val gauge = telemetry.gauge(name, help, labelNames)
      gauge.labels("Germany", "Europe").inc(1)
    }

    "does not try to register duplicates" in new GaugeContext {
      // The CollectorRegistry will not allow duplicates to be registered.
      val secondCounter = telemetry.gauge(name, help, labelNames)
    }

    "returns a gauge with" >> {
      "the correct name" in new GaugeContext {
        CollectorHelper.firstMetricFor(gauge).map { metric =>
          metric.name ==== "test_current_population"
        }
      }

      "the correct help label" in new GaugeContext {
        CollectorHelper.firstMetricFor(gauge).map { metric =>
          metric.help ==== "Current population of a country"
        }
      }

      "the correct label names" in new GaugeContext {
        CollectorHelper.firstSampleFor(gauge).map { sample =>
          sample.labelNames.asScala ==== mutable.Buffer("country", "continent")
        }
      }

      "the correct label values" in new GaugeContext {
        CollectorHelper.firstSampleFor(gauge).map { sample =>
          sample.labelValues.asScala ==== mutable.Buffer("Germany", "Europe")
        }
      }
    }
  }

  "#histogram" >> {

    trait HistogramContext extends Context {
      val name = "request_latency"
      val help = "The latency of a request made"
      val labelNames = Seq("path")
      val gauge = telemetry.histogram(name, help, labelNames)
      gauge.labels("/foo/bar").observe(0.5)
    }

    "does not try to register duplicates" in new HistogramContext {
      // The CollectorRegistry will not allow duplicates to be registered.
      val secondCounter = telemetry.histogram(name, help, labelNames)
    }
  }
}
