package com.samstarling.prometheusfinagle

import com.twitter.finagle.stats._
import io.prometheus.client.{CollectorRegistry, Counter => PCounter, Gauge => PGauge, Histogram => PHistogram}

import scala.collection.concurrent.TrieMap

class PrometheusStatsReceiver(registry: CollectorRegistry, namespace: String) extends StatsReceiver {

  private val counters = TrieMap.empty[String, PCounter]
  private val histograms = TrieMap.empty[String, PHistogram]
  private val gauges = TrieMap.empty[String, PGauge]

  private val helpMessage = "Help is not currently available"

  override def repr: AnyRef = this

  override def counter(verbosity: Verbosity, name: String*): Counter = {
    val (metricName, labels) = extractLabels(name)

    new Counter {
      override def incr(delta: Long): Unit = {
        counters.getOrElseUpdate(metricName, newCounter(metricName, labels.keys.toSeq)).labels(labels.values.toSeq: _*).inc(delta)
      }
    }
  }

  override def stat(verbosity: Verbosity, name: String*): Stat = {
    val (metricName, labels) = extractLabels(name)
    new Stat {
      override def add(value: Float): Unit = {
        histograms.getOrElseUpdate(metricName, newHistogram(metricName, labels.keys.toSeq)).labels(labels.values.toSeq: _*).observe(value)
      }
    }
  }

  override def addGauge(verbosity: Verbosity, name: String*)(f: => Float): Gauge = {
    val (metricName, labels) = extractLabels(name)
    gauges.getOrElseUpdate(metricName, newGauge(metricName, labels.keys.toSeq)).labels(labels.values.toSeq: _*).set(f)

    new Gauge {
      override def remove(): Unit = gauges.remove(metricName)
    }
  }

  private def newCounter(metricName: String, labelNames: Seq[String]): PCounter = {
    PCounter.build()
      .namespace(namespace)
      .name(metricName)
      .labelNames(labelNames: _*)
      .help(helpMessage)
      .register(registry)
  }

  private def newHistogram(metricName: String, labelNames: Seq[String]): PHistogram = {
    PHistogram.build()
      .namespace(namespace)
      .name(metricName)
      .labelNames(labelNames:_*)
      .buckets(0, 1, 2, 3, 4, 5) // TODO: Which buckets does Finagle use?
      .help(helpMessage)
      .register(registry)
  }

  private def newGauge(metricName: String, labelNames: Seq[String]): PGauge = {
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

  def metricPattern: DefaultMetricPatterns.Pattern = DefaultMetricPatterns.All

  protected def extractLabels(name: Seq[String]): (String, Map[String, String]) = {
    metricPattern.applyOrElse(name, (x: Seq[String]) => sanitizeName(x) -> Map.empty)
  }


}

object DefaultMetricPatterns {
  type Pattern = PartialFunction[Seq[String], (String, Map[String, String])]

  val prometheusLabelForLabel = "serviceName"

  val Core: Pattern = {
    case Seq(label, metric) =>
      (metric, Map(prometheusLabelForLabel -> label))
    case Seq(label, "request_payload_bytes") =>
      ("request_payload_bytes", Map(prometheusLabelForLabel -> label))
    case Seq(label, "response_payload_bytes") =>
      ("response_payload_bytes", Map(prometheusLabelForLabel -> label))
    case Seq(label, "retries", metric @ ("request_limit" | "budget_exhausted" | "requeues_per_request" | "budget" | "requeues" | "cannot_retry" | "not_open")) =>
      (s"retries_$metric", Map(prometheusLabelForLabel -> label))
    case Seq(label, "nack_admission_control", metric @ ("dropped_requests")) =>
      (s"nack_admission_control_$metric", Map(prometheusLabelForLabel -> label))
    case Seq(label, "failfast", metric @ ("unhealthy_for_ms" | "marked_dead" | "marked_available" | "unhealthy_num_tries")) =>
      (s"failfast_$metric", Map(prometheusLabelForLabel -> label))
    case Seq(label, "dtab", metric @ ("size")) =>
      (s"dtab_$metric", Map(prometheusLabelForLabel -> label))
    case Seq(label, "dispatcher", "serial", "queue_size") =>
      ("dispatcher_serial_queue_size", Map(prometheusLabelForLabel -> label))
    case Seq(label, "service_creation", "service_acquisition_latency_ms") =>
      ("service_creation_service_acquisition_latency_ms", Map(prometheusLabelForLabel -> label))
  }

  val FailureAccrual: Pattern = {
    case Seq(label, "failure_accrual", metric@("removals" | "revivals" | "probes" | "removed_for_ms")) =>
      (s"loadbalancer_$metric", Map(prometheusLabelForLabel -> label))
  }

  val LoadBalancer: Pattern = {
    case Seq(label, "loadbalancer", metric@("busy"|"available"|"size"|"adds"|"meanweight"|"closed"|"load"|"rebuilds"|"updates"|"max_effort_exhausted"|"removes")) =>
      (s"loadbalancer_$metric", Map(prometheusLabelForLabel -> label))
    case Seq(label, "loadbalancer", "algorithm", algorithm) =>
      (s"loadbalancer_algorithm", Map(prometheusLabelForLabel -> label, "algorithm" -> algorithm))
  }

  val Http: Pattern = {
    case Seq(label, "http", "time", resultCode) =>
      ("http_request_duration", Map(prometheusLabelForLabel -> label, "resultCode" -> resultCode))
    case Seq(label, "http", "status", resultCode) =>
      ("http_request_classification", Map(prometheusLabelForLabel -> label, "resultCode" -> resultCode))
    case Seq(label, "http", "response_size") =>
      ("http_response_size", Map(prometheusLabelForLabel -> label))
  }

  val All: Pattern =
    Core
      .orElse(LoadBalancer)
      .orElse(FailureAccrual)
      .orElse(Http)
}