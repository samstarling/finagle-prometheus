package com.samstarling.prometheusfinagle.metrics

import io.prometheus.client._

import scala.collection.concurrent.TrieMap

// TODO: Make namespace optional
class Telemetry(registry: CollectorRegistry, namespace: String) {

  private val counters = TrieMap.empty[String, Counter]
  private val histograms = TrieMap.empty[String, Histogram]
  private val gauges = TrieMap.empty[String, Gauge]

  private def cacheKeyFor(name: String): String = s"${namespace}_$name"

  // TODO: Support injecting default labels

  def counter(name: String,
              help: String = "No help provided",
              labelNames: Seq[String] = Seq.empty): Counter = {
    counters.getOrElseUpdate(cacheKeyFor(name), {
      Counter
        .build()
        .namespace(namespace)
        .name(name)
        .help(help)
        .labelNames(labelNames: _*)
        .register(registry)
    })
  }

  def histogram(name: String,
                help: String = "No help provided",
                labelNames: Seq[String] = Seq.empty,
                buckets: Seq[Double] = Seq(0.1, 0.5, 1.0, 5.0)): Histogram = {
    histograms.getOrElseUpdate(cacheKeyFor(name), {
      Histogram
        .build()
        .namespace(namespace)
        .name(name)
        .help(help)
        .buckets(buckets: _*)
        .labelNames(labelNames: _*)
        .register(registry)
    })
  }

  def gauge(name: String,
            help: String = "No help provided",
            labelNames: Seq[String] = Seq.empty): Gauge = {
    gauges.getOrElseUpdate(cacheKeyFor(name), {
      Gauge
        .build()
        .namespace(namespace)
        .name(name)
        .help(help)
        .labelNames(labelNames: _*)
        .register(registry)
    })
  }
}
