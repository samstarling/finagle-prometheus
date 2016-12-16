package com.samstarling.prometheusfinagle.mapper

import com.twitter.common.metrics.Snapshot
import io.prometheus.client.Collector
import io.prometheus.client.Collector.MetricFamilySamples
import io.prometheus.client.Collector.MetricFamilySamples.Sample

import scala.collection.JavaConverters._

protected class HistogramMapper {
  def apply(rawName: String, snapshot: Snapshot): List[MetricFamilySamples] = {

    val name = sanitizeName(rawName)
    val percentileMetric = new MetricFamilySamples(name, Collector.Type.GAUGE, "No help", percentileSamples(name, snapshot))
    val minMetric = new MetricFamilySamples(s"${name}__max", Collector.Type.GAUGE, "No help", sampleWithValue(name, snapshot.max))
    val maxMetric = new MetricFamilySamples(s"${name}__min", Collector.Type.GAUGE, "No help", sampleWithValue(name, snapshot.min))
    val countMetric = new MetricFamilySamples(s"${name}__count", Collector.Type.GAUGE, "No help", sampleWithValue(name, snapshot.count))
    val sumMetric = new MetricFamilySamples(s"${name}__sum", Collector.Type.GAUGE, "No help", sampleWithValue(name, snapshot.sum))
    List(percentileMetric, minMetric, maxMetric, countMetric, sumMetric)
  }

  private def sampleWithValue(name: String, value: Number) = {
    List(new Sample(
      sanitizeName(name),
      List().asJava,
      List().asJava,
      value.doubleValue()
    )).asJava
  }

  private def percentileSamples(name: String, snapshot: Snapshot) = {
    snapshot.percentiles().map { percentile =>
      new Sample(
        sanitizeName(name),
        List("percentile").asJava,
        List(percentile.getQuantile.toString).asJava,
        percentile.getValue.toDouble
      )
    }.toList.asJava
  }

  private def sanitizeName(name: String) = {
    name.replace("/", "__").replace(".", "_")
  }
}
