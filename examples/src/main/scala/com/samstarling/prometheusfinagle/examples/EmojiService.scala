package com.samstarling.prometheusfinagle.examples

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.finagle.stats.StatsReceiver
import com.twitter.util.Future

class EmojiService(statsReceiver: StatsReceiver) extends Service[Request, Response] {

  private val client = Http.client
    .withTls("api.github.com")
    .withStatsReceiver(statsReceiver)
    .newService("api.github.com:443")

  private val emojiRequest = Request(Method.Get, "/emojis")
  emojiRequest.headerMap.add("User-Agent", "My-Finagle-Example")

  override def apply(request: Request): Future[Response] = {
    client.apply(emojiRequest).map { resp =>
      println(resp)
      val r = Response(request.version, Status.Ok)
      r.setContentString(s"Emojis: ${resp.getContentString()}")
      r
    }
  }
}
