package com.samstarling.prometheusfinagle

import com.twitter.finagle.stats._
import io.prometheus.client.{CollectorRegistry, Summary, Counter => PCounter, Gauge => PGauge}
import scala.collection.concurrent.TrieMap

import com.twitter.finagle.util.DefaultTimer
import com.twitter.util._

class PrometheusStatsReceiver(registry: CollectorRegistry, namespace: String, timer: Timer, gaugePollInterval: Duration)
  extends StatsReceiver with Closable {

  def this() = this(CollectorRegistry.defaultRegistry, "finagle", DefaultTimer.getInstance, Duration.fromSeconds(10))

  def this(registry: CollectorRegistry) = this(registry, "finagle", DefaultTimer.getInstance, Duration.fromSeconds(10))

  protected val counters = TrieMap.empty[String, PCounter]
  protected val summaries = TrieMap.empty[String, Summary]
  protected val gauges = TrieMap.empty[String, PGauge]
  protected val gaugeChilds = TrieMap.empty[(String, Seq[String]), PGauge.Child]
  protected val gaugeProviders = TrieMap.empty[(String, Seq[String]), (() => Float)]

  protected val task = timer.schedule(gaugePollInterval) {
    gaugeProviders.foreach { case (childGaugeKey, provider) =>
      gaugeChilds.get(childGaugeKey).foreach(_.set(provider()))
    }
  }

  override def close(deadline: Time): Future[Unit] = task.close(deadline)

  // TODO: Map name (Seq[String]) to a meaningful help string
  private val helpMessage =
    "Refer to https://twitter.github.io/finagle/guide/Metrics.html"

  override def repr: AnyRef = this

  override def counter(verbosity: Verbosity, name: String*): Counter = {
    val (metricName, labels) = extractLabels(name)
    val c = counters
      .getOrElseUpdate(metricName, this.synchronized { newCounter(metricName, labels.keys.toSeq) })
      .labels(labels.values.toSeq: _*)

    new Counter {
      override def incr(delta: Long): Unit = {
        c.inc(delta)
      }
    }
  }

  override def stat(verbosity: Verbosity, name: String*): Stat = {
    val (metricName, labels) = extractLabels(name)
    val s = summaries
      .getOrElseUpdate(metricName, this.synchronized { newSummary(metricName, labels.keys.toSeq) })
      .labels(labels.values.toSeq: _*)

    new Stat {
      override def add(value: Float): Unit = {
        s.observe(value)
      }
    }
  }

  override def addGauge(verbosity: Verbosity, name: String*)(
                        f: => Float): Gauge = {
    val (metricName, labels) = extractLabels(name)
    gauges
      .getOrElseUpdate(metricName, this.synchronized { newGauge(metricName, labels.keys.toSeq) })
    val labelValues = labels.values.toSeq
    gaugeChilds
      .getOrElseUpdate((metricName, labelValues), this.synchronized { gauges(metricName).labels(labelValues: _*) })
      .set(f)   // Set once initially
    gaugeProviders.update((metricName, labelValues), () => f)
    new Gauge {
      override def remove(): Unit = gaugeProviders.remove((metricName, labelValues))
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

  protected def extractLabels(name: Seq[String]): (String, Map[String, String]) = {
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
