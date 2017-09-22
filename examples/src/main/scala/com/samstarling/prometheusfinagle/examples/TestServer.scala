package com.samstarling.prometheusfinagle.examples

import java.net.InetSocketAddress

import com.samstarling.prometheusfinagle.PrometheusStatsReceiver
import com.samstarling.prometheusfinagle.metrics.MetricsService
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http._
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.service.{NotFoundService, RoutingService}
import com.twitter.finagle.util.LoadService
import com.twitter.finagle.{Http, Service}
import io.prometheus.client.CollectorRegistry

object TestServer extends App {

  val registry = new CollectorRegistry(true)
  val statsReceiver = new PrometheusStatsReceiver(registry, "foo")

  val router: Service[Request, Response] = RoutingService.byMethodAndPathObject {
    case (Method.Get, Root / "emoji") => new EmojiService(statsReceiver)
    case (Method.Get, Root / "metrics") => new MetricsService(registry)
    case (Method.Get, Root / "echo") => new EchoService
    case _ => new NotFoundService
  }

  ServerBuilder()
    .stack(Http.server)
    .name("admin")
    .bindTo(new InetSocketAddress(8080))
    .build(router)
}
