package com.samstarling.prometheusfinagle.examples

import java.net.InetSocketAddress

import com.samstarling.prometheusfinagle.filter.{HttpLatencyMonitoringFilter, HttpMonitoringFilter}
import com.samstarling.prometheusfinagle.mapper.FinagleToPrometheusMapper
import com.samstarling.prometheusfinagle.metrics.{MetricsService, Telemetry}
import com.twitter.common.metrics.Metrics
import com.twitter.finagle.Service
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.http._
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.stats.JsonExporter
import io.prometheus.client.CollectorRegistry

import scala.collection.JavaConverters._

object TestServer extends App {

  val prometheusMapper = new FinagleToPrometheusMapper(Metrics.root)
  val registry = new CollectorRegistry(true)
  val telemetry = new Telemetry(registry, "MyServer")
  val monitoringFilter = new HttpMonitoringFilter(telemetry)
  val latencyMonitoringFilter = new HttpLatencyMonitoringFilter(telemetry, Seq(5.0, 10.0))

  private def allMetrics = {
    (registry.metricFamilySamples.asScala ++ prometheusMapper.metricFamilySamples.toList).toList
  }

  val metricsService = new MetricsService(allMetrics)

  val routingService: Service[Request, Response] = RoutingService.byMethodAndPathObject {
    case (Method.Get, Root / "hello" / name) => new EchoService(s"Hello ${name}")
    case (Method.Get, Root / "metrics") => metricsService
    case (Method.Get, Root / "finagle-metrics") => new JsonExporter(Metrics.root)
    case _ => new EchoService("Fallback")
  }

  val server: Server = ServerBuilder()
    .codec(Http())
    .bindTo(new InetSocketAddress(8080))
    .name("HttpServer")
    .build(latencyMonitoringFilter andThen monitoringFilter andThen routingService)
}
