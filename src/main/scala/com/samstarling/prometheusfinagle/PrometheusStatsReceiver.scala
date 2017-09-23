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
      val (_, labelValues) = extractLabelValues(name)
      counters.getOrElseUpdate(name, newCounter(name)).labels(labelValues: _*).inc(delta)
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
    val (metricName, labelNames) =
      extractLabelNames(name)
    PCounter.build()
      .namespace(namespace)
      .name(metricName)
      .labelNames(labelNames: _*)
      .help(helpMessage)
      .register(registry)
  }

  private def newHistogram(name: Seq[String]): PHistogram = {
    val (metricName, labelNames) =
      extractLabelNames(name)

    PHistogram.build()
      .namespace(namespace)
      .name(metricName)
      .labelNames(labelNames:_*)
      .buckets(0, 1, 2, 3, 4, 5) // TODO: Which buckets does Finagle use?
      .help(helpMessage)
      .register(registry)
  }

  private def newGauge(name: Seq[String]): PGauge = {
    val (metricName, labelNames) =
      extractLabelNames(name)

    PGauge.build()
      .namespace(namespace)
      .name(metricName)
      .labelNames(labelNames:_*)
      .help(helpMessage)
      .register(registry)
  }

  private def sanitizeName(name: Seq[String]): String = {
    name.map(_.replaceAll("[^\\w]", "_")).mkString("__")
  }

  protected def extractLabelNames(name: Seq[String]): (String, Seq[String]) = name match {
    case Seq(label, "requests") =>
      ("requests", Seq("serviceName"))
    case default => (sanitizeName(default), Seq.empty[String])
  }

  protected def extractLabelValues(name: Seq[String]): (String, Seq[String]) = name match {
    case Seq(label, "requests") =>
      ("requests", Seq(label))
    case default => (sanitizeName(default), Seq.empty[String])
  }

}
