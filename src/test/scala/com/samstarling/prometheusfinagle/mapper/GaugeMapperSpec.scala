package com.samstarling.prometheusfinagle.mapper

import com.samstarling.prometheusfinagle.UnitTest
import org.specs2.specification.Scope

import scala.collection.JavaConverters._

class GaugeMapperSpec extends UnitTest {

  trait Context extends Scope {
    val mapper = new GaugeMapper()
    val metric = mapper.apply("WaterMonitor/current.levels/sea_level", 6.25)
  }

  "apply" >> {
    "produces a metric with a sanitized name" in new Context {
      metric.name ==== "WaterMonitor__current_levels__sea_level"
    }

    "produces a metric with a single sample" in new Context {
      metric.samples must have size(1)
    }

    "produces a sample with a sanitized name" in new Context {
      val sample = metric.samples.get(0)
      sample.name ==== "WaterMonitor__current_levels__sea_level"
    }

    "produces a sample with correct value" in new Context {
      val sample = metric.samples.get(0)
      sample.value === 6.25
    }

    // Finagle metrics don't have labels, just a name and a value
    "produces a sample with no label names" in new Context {
      val sample = metric.samples.get(0)
      sample.labelNames.asScala.toList ==== List.empty
    }

    "produces a sample with no label values" in new Context {
      val sample = metric.samples.get(0)
      sample.labelValues.asScala.toList ==== List.empty
    }
  }
}
