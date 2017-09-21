package com.samstarling.prometheusfinagle.mapper

import io.prometheus.client.Collector
import io.prometheus.client.Collector.MetricFamilySamples
import io.prometheus.client.Collector.MetricFamilySamples.Sample

import scala.collection.JavaConverters._

protected class GaugeMapper {
  def apply(name: String, value: Number): MetricFamilySamples = {
    val sample = new Sample(sanitizeName(name),
                            List.empty.asJava,
                            List.empty.asJava,
                            value.doubleValue())
    new MetricFamilySamples(sample.name,
                            Collector.Type.GAUGE,
                            "No help",
                            List(sample).asJava)
  }

  private def sanitizeName(name: String) = {
    name.replace("/", "__").replace(".", "_")
  }
}
