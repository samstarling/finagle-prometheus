package com.samstarling.prometheusfinagle.mapper

import com.twitter.common.metrics.Metrics
import io.prometheus.client.Collector.MetricFamilySamples

import scala.collection.JavaConverters._

class FinagleToPrometheusMapper(metrics: Metrics) {

  private def gauges = metrics.sampleGauges().asScala
  private def histograms = metrics.sampleHistograms().asScala
  private def counters = metrics.sampleCounters().asScala

  def metricFamilySamples: Iterable[MetricFamilySamples] = {
    gaugeSamples ++ histogramSamples ++ counterSamples
  }

  private def gaugeSamples: Iterable[MetricFamilySamples] = {
    gauges.map {
      case (name, value) => new GaugeMapper().apply(name, value)
    }.toList
  }

  private def histogramSamples: Iterable[MetricFamilySamples] = {
    histograms
      .map {
        case (name, snapshot) => new HistogramMapper().apply(name, snapshot)
      }
      .toList
      .flatten
  }

  private def counterSamples: Iterable[MetricFamilySamples] = {
    counters.map {
      case (name, value) => new CounterMapper().apply(name, value)
    }.toList
  }
}
