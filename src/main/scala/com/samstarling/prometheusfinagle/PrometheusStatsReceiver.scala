package com.samstarling.prometheusfinagle

import com.twitter.finagle.stats._
import io.prometheus.client.{CollectorRegistry, Counter => PCounter, Gauge => PGauge, Histogram => PHistogram}

import scala.collection.concurrent.TrieMap

class PrometheusStatsReceiver(registry: CollectorRegistry, namespace: String) extends StatsReceiver {

  private val counters = TrieMap.empty[Seq[String], PCounter]
  private val histograms = TrieMap.empty[Seq[String], PHistogram]
  private val gauges = TrieMap.empty[Seq[String], PGauge]

  private val helpMessage = "Help is not currently available"

  override def repr: AnyRef = this

  override def counter(verbosity: Verbosity, name: String*): Counter = new Counter {
    override def incr(delta: Long): Unit = {
      counters.getOrElseUpdate(name, newCounter(name)).inc(delta)
    }
  }

  override def stat(verbosity: Verbosity, name: String*): Stat = new Stat {
    override def add(value: Float): Unit = {
      histograms.getOrElseUpdate(name, newHistogram(name)).observe(value)
    }
  }

  override def addGauge(verbosity: Verbosity, name: String*)(f: => Float): Gauge = new Gauge {
    gauges.getOrElseUpdate(name, newGauge(name)).set(f)
    override def remove(): Unit = gauges.remove(name)
  }

  private def newCounter(name: Seq[String]): PCounter = {
    PCounter.build()
      .namespace(namespace)
      .name(sanitizeName(name))
      .help(helpMessage)
      .register(registry)
  }

  private def newHistogram(name: Seq[String]): PHistogram = {
    PHistogram.build()
      .namespace(namespace)
      .name(sanitizeName(name))
      .buckets(0, 1, 2, 3, 4, 5) // TODO: Which buckets does Finagle use?
      .help(helpMessage)
      .register(registry)
  }

  private def newGauge(name: Seq[String]): PGauge = {
    PGauge.build()
      .namespace(namespace)
      .name(sanitizeName(name))
      .help(helpMessage)
      .register(registry)
  }

  private def sanitizeName(name: Seq[String]) = {
    name.map(_.replaceAll("[^\\w]", "_")).mkString("__")
  }
}
