package com.samstarling.prometheusfinagle.mapper

import com.samstarling.prometheusfinagle.UnitTest
import org.specs2.specification.Scope

import scala.collection.JavaConverters._

class CounterMapperSpec extends UnitTest {

  trait Context extends Scope {
    val mapper = new CounterMapper()
    val metric = mapper.apply("HttpServer/foo.bar/total_widgets", 365.24)
  }

  "apply" >> {
    "produces a metric with a sanitized name" in new Context {
      metric.name ==== "HttpServer__foo_bar__total_widgets"
    }

    "produces a metric with a single sample" in new Context {
      metric.samples must have size(1)
    }

    "produces a sample with a sanitized name" in new Context {
      val sample = metric.samples.get(0)
      sample.name ==== "HttpServer__foo_bar__total_widgets"
    }

    "produces a sample with correct value" in new Context {
      val sample = metric.samples.get(0)
      sample.value === 365.24
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
