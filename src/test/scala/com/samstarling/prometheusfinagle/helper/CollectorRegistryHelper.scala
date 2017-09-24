package com.samstarling.prometheusfinagle.helper

import io.prometheus.client.Collector.MetricFamilySamples.{
  Sample => PrometheusSample
}
import io.prometheus.client.{Collector, CollectorRegistry}

import scala.collection.JavaConverters._

case class CollectorRegistryHelper(registry: CollectorRegistry) {

  // TODO: Messy
  def samples: Map[String, List[Sample]] = {
    def metricFamilies = registry.metricFamilySamples.asScala.toList
    def allSamples: List[List[Sample]] =
      metricFamilies.map(_.samples.asScala.toList.map(Sample(_)))
    def flatSamples: List[Sample] = allSamples.flatten
    flatSamples
      .map({ s =>
        s.name -> s
      })
      .groupBy(_._1)
      .mapValues(_.map(_._2))
  }
}

case class Metric(metric: Collector.MetricFamilySamples) {
  def samples: Map[String, Sample] = {
    metric.samples.asScala.toList
      .map(sample => sample.name -> Sample(sample))
      .toMap
  }
}

case class Sample(sample: PrometheusSample) {
  def name: String = sample.name
  def value: Double = sample.value
  def dimensions: Map[String, String] = {
    sample.labelNames.asScala.zip(sample.labelValues.asScala).toMap
  }
}
