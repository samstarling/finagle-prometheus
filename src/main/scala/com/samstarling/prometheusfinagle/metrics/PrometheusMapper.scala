package com.samstarling.prometheusfinagle.metrics

import com.twitter.common.metrics.Metrics
import io.prometheus.client.Collector
import io.prometheus.client.Collector.MetricFamilySamples
import io.prometheus.client.Collector.MetricFamilySamples.Sample

import scala.collection.JavaConverters._

class PrometheusMapper(registry: Metrics) {

  private def gauges = registry.sampleGauges().asScala
  private def histograms = registry.sampleHistograms().asScala
  private def counters = registry.sampleCounters().asScala

  def metricFamilySamples: Iterable[MetricFamilySamples] = {
    familySamplesForGauges ++ familySamplesForHistograms ++ familySamplesForCounters
  }

  private def familySamplesForGauges = {
    gauges.map { case (name, value) =>
      val sample = new Sample(sanitizeName(name), List.empty.asJava, List.empty.asJava, value.doubleValue())
      new MetricFamilySamples(sample.name, Collector.Type.GAUGE, "No help", List(sample).asJava)
    }
  }

  private def familySamplesForHistograms = {
    histograms.map { case (metricName, metricValue) =>
      val samples = metricValue.percentiles().map { percentile =>
        new Sample(
          sanitizeName(metricName),
          List("quantile").asJava,
          List("p" + (percentile.getQuantile * 100).toString).asJava,
          percentile.getValue.toDouble
        )
      }.toList
      new MetricFamilySamples(sanitizeName(metricName), Collector.Type.GAUGE, "No help", samples.asJava)
    }
  }

  private def familySamplesForCounters = {
    counters.map { case (metricName, metricValue) =>
      val sample = new Sample(sanitizeName(metricName), List.empty.asJava, List.empty.asJava, metricValue.doubleValue())
      new MetricFamilySamples(sample.name, Collector.Type.COUNTER, "No help", List(sample).asJava)
    }
  }

  private def sanitizeName(name: String) = {
    name.replace("/", "__").replace(".", "_")
  }
}
