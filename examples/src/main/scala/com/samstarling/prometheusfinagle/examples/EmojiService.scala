package com.samstarling.prometheusfinagle.examples

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.finagle.stats.StatsReceiver
import com.twitter.util.Future

class EmojiService(statsReceiver: StatsReceiver) extends Service[Request, Response] {

  val client = Http.client
    .withStatsReceiver(statsReceiver)
    .newService("api.github.com:80")

  override def apply(request: Request): Future[Response] = {
    client.apply(Request(Method.Get, "/emojis")).map { resp =>
      val r = Response(request.version, Status.Ok)
      r.setContentString(s"Emojis: ${resp.getContentString()}")
      r
    }
  }
}
