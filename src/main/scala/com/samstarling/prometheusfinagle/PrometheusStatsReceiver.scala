package com.samstarling.prometheusfinagle

import com.twitter.finagle.stats._
import io.prometheus.client.{CollectorRegistry, Counter => PCounter, Gauge => PGauge, Histogram => PHistogram}

import scala.collection.concurrent.TrieMap

class PrometheusStatsReceiver(registry: CollectorRegistry,
                              namespace: String,
                              labeller: MetricLabeller = new MetricLabeller) extends StatsReceiver {

  private val counters = TrieMap.empty[String, PCounter]
  private val histograms = TrieMap.empty[String, PHistogram]
  private val gauges = TrieMap.empty[String, PGauge]

  // TODO: Map name (Seq[String]) to a meaningful help string
  private val helpMessage = "Help is not currently available"

  override def repr: AnyRef = this

  override def counter(verbosity: Verbosity, name: String*): Counter = new Counter {
    override def incr(delta: Long): Unit = {
      // TODO: Calling sanitizeName twice could be nicer, but we need to avoid registering duplicate metrics
      // with the same name in the CollectorRegistry
      counters.getOrElseUpdate(labeller.sanitizeName(name), newCounter(labeller.sanitizeName(name), labeller.labelNamesFor(name)))
        .labels(labeller.labelsFor(name): _*)
        .inc(delta)
    }
  }

  override def stat(verbosity: Verbosity, name: String*): Stat = new Stat {
    override def add(value: Float): Unit = {
      histograms.getOrElseUpdate(labeller.sanitizeName(name), newHistogram(labeller.sanitizeName(name), labeller.labelNamesFor(name)))
        .labels(labeller.labelsFor(name): _*)
        .observe(value)
    }
  }

  override def addGauge(verbosity: Verbosity, name: String*)(f: => Float): Gauge = new Gauge {
    gauges.getOrElseUpdate(labeller.sanitizeName(name), newGauge(labeller.sanitizeName(name), labeller.labelNamesFor(name)))
      .labels(labeller.labelsFor(name): _*)
      .set(f)
    override def remove(): Unit = gauges.remove(labeller.sanitizeName(name))
  }

  // TODO: Can the following three methods be generic?
  private def newCounter(name: String, labelNames: Seq[String]): PCounter = {
    PCounter.build()
      .namespace(namespace)
      .name(name)
      .labelNames(labelNames: _*)
      .help(helpMessage)
      .register(registry)
  }

  private def newHistogram(name: String, labelNames: Seq[String]): PHistogram = {
    PHistogram.build()
      .namespace(namespace)
      .name(name)
      .labelNames(labelNames: _*)
      .buckets(0, 1, 2, 3, 4, 5) // TODO: Map name (Seq[String]) to bucket configuration
      .help(helpMessage)
      .register(registry)
  }

  private def newGauge(name: String, labelNames: Seq[String]): PGauge = {
    PGauge.build()
      .namespace(namespace)
      .name(name)
      .labelNames(labelNames: _*)
      .help(helpMessage)
      .register(registry)
  }
}
