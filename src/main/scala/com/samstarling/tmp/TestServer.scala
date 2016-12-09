package com.samstarling.tmp

import java.net.InetSocketAddress

import com.samstarling.filter.{HttpLatencyMonitoringFilter, HttpMonitoringFilter}
import com.samstarling.metrics.Telemetry
import com.samstarling.service.MetricsService
import com.twitter.finagle.Service
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http._
import com.twitter.finagle.http.path._
import com.twitter.util.Future
import io.prometheus.client.CollectorRegistry

object TestServer extends App {
  def echo(message: String): Service[Request, Response] = new Service[Request, Response] {
    def apply(req: Request): Future[Response] = {
      val rep = Response(req.version, Status.Ok)
      rep.setContentString(s"$message, and the ID was ${req.params.get("id")}")
      Future(rep)
    }
  }

  val registry = CollectorRegistry.defaultRegistry
  val telemetry = new Telemetry(registry, "foo")
  val monitoringFilter = new HttpMonitoringFilter(telemetry)
  val latencyMonitoringFilter = new HttpLatencyMonitoringFilter(telemetry, Seq(5.0, 10.0))
  val metricsService = new MetricsService(registry)

  val routingService = RoutingService.byMethodAndPathObject {
    case (Method.Get, Root / "hello" / name) => echo(s"Hello ${name}")
    case (Method.Get, Root / "metrics") => metricsService
    case _ => echo("Fallback")
  }

  val server: Server = ServerBuilder()
    .codec(Http())
    .bindTo(new InetSocketAddress(8080))
    .name("HttpServer")
    .build(latencyMonitoringFilter andThen monitoringFilter andThen routingService)
}
