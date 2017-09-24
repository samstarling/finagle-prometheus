package com.samstarling.prometheusfinagle

import com.twitter.finagle.stats._
import io.prometheus.client.{CollectorRegistry, Counter => PCounter, Gauge => PGauge, Histogram => PHistogram}

import scala.collection.concurrent.TrieMap

class PrometheusStatsReceiver(registry: CollectorRegistry,
                              namespace: String,
                              labeller: MetricLabeller = new MetricLabeller) extends StatsReceiver {

  private val counters = TrieMap.empty[Seq[String], PCounter]
  private val histograms = TrieMap.empty[Seq[String], PHistogram]
  private val gauges = TrieMap.empty[Seq[String], PGauge]

  // TODO: Map name (Seq[String]) to a meaningful help string
  private val helpMessage = "Help is not currently available"

  override def repr: AnyRef = this

  override def counter(verbosity: Verbosity, name: String*): Counter = new Counter {
    override def incr(delta: Long): Unit = {
      counters.getOrElseUpdate(name, newCounter(name))
        .labels(labeller.labelsFor(name): _*)
        .inc(delta)
    }
  }

  override def stat(verbosity: Verbosity, name: String*): Stat = new Stat {
    override def add(value: Float): Unit = {
      histograms.getOrElseUpdate(name, newHistogram(name))
        .labels(labeller.labelsFor(name): _*)
        .observe(value)
    }
  }

  override def addGauge(verbosity: Verbosity, name: String*)(f: => Float): Gauge = new Gauge {
    gauges.getOrElseUpdate(name, newGauge(name))
      .labels(labeller.labelsFor(name): _*)
      .set(f)
    override def remove(): Unit = gauges.remove(name)
  }

  // TODO: Can the following three methods be generic?
  private def newCounter(rawName: Seq[String]): PCounter = {
    val (name, labelNames) = labeller.labelNamesFor(rawName)
    PCounter.build()
      .namespace(namespace)
      .name(name)
      .labelNames(labelNames: _*)
      .help(helpMessage)
      .register(registry)
  }

  private def newHistogram(rawName: Seq[String]): PHistogram = {
    val (name, labelNames) = labeller.labelNamesFor(rawName)
    PHistogram.build()
      .namespace(namespace)
      .name(name)
      .labelNames(labelNames: _*)
      .buckets(0, 1, 2, 3, 4, 5) // TODO: Map name (Seq[String]) to bucket configuration
      .help(helpMessage)
      .register(registry)
  }

  private def newGauge(rawName: Seq[String]): PGauge = {
    val (name, labelNames) = labeller.labelNamesFor(rawName)
    PGauge.build()
      .namespace(namespace)
      .name(name)
      .labelNames(labelNames: _*)
      .help(helpMessage)
      .register(registry)
  }
}
