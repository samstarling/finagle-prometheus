package com.samstarling.prometheusfinagle.helper

import io.prometheus.client.Collector.MetricFamilySamples.{Sample => PrometheusSample}
import io.prometheus.client.{Collector, Counter, Gauge, Histogram}

import scala.collection.JavaConverters._

object CollectorHelper {
  def firstMetricFor(
      counter: Counter): Option[Collector.MetricFamilySamples] = {
    counter.collect().asScala.toList.headOption
  }

  def firstMetricFor(gauge: Gauge): Option[Collector.MetricFamilySamples] = {
    gauge.collect().asScala.toList.headOption
  }

  def firstMetricFor(
      histogram: Histogram): Option[Collector.MetricFamilySamples] = {
    histogram.collect().asScala.toList.headOption
  }

  def firstSampleFor(counter: Counter): Option[PrometheusSample] = {
    firstMetricFor(counter).flatMap { metric =>
      metric.samples.asScala.toList.headOption
    }
  }

  def firstSampleFor(counter: Gauge): Option[PrometheusSample] = {
    firstMetricFor(counter).flatMap { metric =>
      metric.samples.asScala.toList.headOption
    }
  }

  def firstSampleFor(histogram: Histogram): Option[PrometheusSample] = {
    firstMetricFor(histogram).flatMap { metric =>
      metric.samples.asScala.toList.headOption
    }
  }
}
