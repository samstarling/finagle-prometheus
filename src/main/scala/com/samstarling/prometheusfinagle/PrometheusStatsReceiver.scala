package com.samstarling.prometheusfinagle

import com.twitter.finagle.stats._
import io.prometheus.client.{CollectorRegistry, Summary, Counter => PCounter, Gauge => PGauge}
import scala.collection.concurrent.TrieMap

class PrometheusStatsReceiver(registry: CollectorRegistry,
                              namespace: String = "finagle")
    extends StatsReceiver {

  private val counters = TrieMap.empty[String, PCounter]
  private val summaries = TrieMap.empty[String, Summary]
  private val gauges = TrieMap.empty[String, PGauge]

  // TODO: Map name (Seq[String]) to a meaningful help string
  private val helpMessage =
    "Refer to https://twitter.github.io/finagle/guide/Metrics.html"

  override def repr: AnyRef = this

  override def counter(verbosity: Verbosity, name: String*): Counter = {
    val (metricName, labels) = extractLabels(name)
    new Counter {
      override def incr(delta: Long): Unit = {
        counters
          .getOrElseUpdate(metricName,
                           newCounter(metricName, labels.keys.toSeq))
          .labels(labels.values.toSeq: _*)
          .inc(delta)
      }
    }
  }

  override def stat(verbosity: Verbosity, name: String*): Stat = {
    val (metricName, labels) = extractLabels(name)
    new Stat {
      override def add(value: Float): Unit = {
        summaries
          .getOrElseUpdate(metricName,
                           newSummary(metricName, labels.keys.toSeq))
          .labels(labels.values.toSeq: _*)
          .observe(value)
      }
    }
  }

  override def addGauge(verbosity: Verbosity, name: String*)(
      f: => Float): Gauge = {
    val (metricName, labels) = extractLabels(name)
    gauges
      .getOrElseUpdate(metricName, newGauge(metricName, labels.keys.toSeq))
      .labels(labels.values.toSeq: _*)
      .set(f)
    new Gauge {
      override def remove(): Unit = gauges.remove(metricName)
    }
  }

  private def newCounter(metricName: String,
                         labelNames: Seq[String]): PCounter = {
    PCounter
      .build()
      .namespace(namespace)
      .name(metricName)
      .labelNames(labelNames: _*)
      .help(helpMessage)
      .register(registry)
  }

  private def newSummary(metricName: String,
                         labelNames: Seq[String]): Summary = {
    Summary
      .build()
      .namespace(namespace)
      .name(metricName)
      .labelNames(labelNames: _*)
      .quantile(0.0, 0.0001)
      .quantile(0.5, 0.0001)
      .quantile(0.9, 0.0001)
      .quantile(0.95, 0.0001)
      .quantile(0.99, 0.0001)
      .quantile(0.999, 0.0001)
      .quantile(0.9999, 0.0001)
      .quantile(1, 0.0001)
      .help(helpMessage)
      .register(registry)
  }

  private def newGauge(metricName: String, labelNames: Seq[String]): PGauge = {
    PGauge
      .build()
      .namespace(namespace)
      .name(metricName)
      .labelNames(labelNames: _*)
      .help(helpMessage)
      .register(registry)
  }

  def metricPattern: DefaultMetricPatterns.Pattern = DefaultMetricPatterns.All

  protected def extractLabels(
      name: Seq[String]): (String, Map[String, String]) = {
    metricPattern.applyOrElse(
      name,
      (x: Seq[String]) => DefaultMetricPatterns.sanitizeName(x) -> Map.empty)
  }
}

object DefaultMetricPatterns {
  def sanitizeName(name: Seq[String]): String = {
    name.map(_.replaceAll("[^\\w]", "_")).mkString("_")
  }

  type Pattern = PartialFunction[Seq[String], (String, Map[String, String])]

  val prometheusLabelForLabel = "serviceName"

  val DefaultMatch: Pattern = {
    case label +: metrics =>
      (sanitizeName(metrics), Map(prometheusLabelForLabel -> label))
  }

  val PerHost: Pattern = {
    case Seq("host", label, host, "failures", failure) =>
      (s"perHost_failures",
       Map(prometheusLabelForLabel -> label,
           "host" -> host,
           "class" -> failure))
    case "host" +: label +: host +: metrics =>
      (s"perHost_${sanitizeName(metrics)}",
       Map(prometheusLabelForLabel -> label, "host" -> host))
  }

  val Core: Pattern = {
    case Seq(label, "failures", exceptionName) =>
      ("failures_perException",
       Map(prometheusLabelForLabel -> label, "class" -> exceptionName))
    case Seq(label, "sourcedfailures", sourceService, exceptionName) =>
      ("failures_perException",
       Map(prometheusLabelForLabel -> label,
           "sourceService" -> sourceService,
           "class" -> exceptionName))
    case Seq(label, metric) =>
      (metric, Map(prometheusLabelForLabel -> label))
  }

  val LoadBalancer: Pattern = {
    case Seq(label, "loadbalancer", "algorithm", algorithm) =>
      (s"loadbalancer_algorithm",
       Map(prometheusLabelForLabel -> label, "algorithm" -> algorithm))
  }

  val Http: Pattern = {
    case Seq(label, "http", "time", resultCode) =>
      ("http_request_duration",
       Map(prometheusLabelForLabel -> label, "resultCode" -> resultCode))
    case Seq(label, "http", "status", resultCode) =>
      ("http_request_classification",
       Map(prometheusLabelForLabel -> label, "resultCode" -> resultCode))
    case Seq(label, "http", "response_size") =>
      ("http_response_size", Map(prometheusLabelForLabel -> label))
  }

  val All: Pattern =
    Core
      .orElse(LoadBalancer)
      .orElse(Http)
      .orElse(PerHost)
      .orElse(DefaultMatch)
}
