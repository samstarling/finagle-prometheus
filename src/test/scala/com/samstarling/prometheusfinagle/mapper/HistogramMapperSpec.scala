package com.samstarling.prometheusfinagle.mapper

import com.samstarling.prometheusfinagle.UnitTest
import com.twitter.common.metrics.{Metrics, Percentile, Snapshot}
import org.specs2.specification.Scope

import scala.collection.JavaConverters._

class HistogramMapperSpec extends UnitTest {

  class TestSnapshot extends Snapshot {
    override def count(): Long = 10
    override def max(): Long = 11
    override def avg(): Double = 10
    override def stddev(): Double = 1
    override def min(): Long = 9
    override def sum(): Long = 100
    override def percentiles(): Array[Percentile] =
      List(
        new Percentile(0.9999, 100L),
        new Percentile(0.99, 90L),
        new Percentile(0.50, 50L)
      ).toArray
  }

  trait Context extends Scope {
    val mapper = new HistogramMapper()
    val snapshot = new TestSnapshot
    val metrics = mapper.apply("HttpClient/latencies", snapshot)
    val metricsByName = metrics.groupBy(_.name)
  }

  "apply" >> {

    "produces a 'percentile' metric" >> {
      "with a sample for each percentile" in new Context {
        val quantileMetric = metricsByName.get("HttpClient__latencies").get.head
        quantileMetric.samples must have size (3)
      }

      "where each sample has a sanitized name" in new Context {
        val quantileMetric = metricsByName.get("HttpClient__latencies").get.head
        val sample = quantileMetric.samples.get(0)
        sample.name ==== "HttpClient__latencies"
      }

      "where the samples have the percentile as a label" in new Context {
        val percentileMetric =
          metricsByName.get("HttpClient__latencies").get.head
        val sample = percentileMetric.samples.get(0)
        sample.labelNames.asScala.toList ==== List("percentile")
        sample.labelValues.asScala.toList ==== List("0.9999")
      }
    }

    "produces a 'sum' metric" >> {
      "with the correct value" in new Context {
        val metric = metricsByName.get("HttpClient__latencies__sum").get.head
        val sample = metric.samples.get(0)
        sample.value ==== 100
      }
    }

    "produces a 'count' metric" >> {
      "with the correct value" in new Context {
        val metric = metricsByName.get("HttpClient__latencies__count").get.head
        val sample = metric.samples.get(0)
        sample.value ==== 10
      }
    }

    "produces a 'min' metric" >> {
      "with the correct value" in new Context {
        val metric = metricsByName.get("HttpClient__latencies__min").get.head
        val sample = metric.samples.get(0)
        sample.value ==== 9
      }
    }

    "produces a 'max' metric" >> {
      "with the correct value" in new Context {
        val metric = metricsByName.get("HttpClient__latencies__max").get.head
        val sample = metric.samples.get(0)
        sample.value ==== 11
      }
    }
  }
}
