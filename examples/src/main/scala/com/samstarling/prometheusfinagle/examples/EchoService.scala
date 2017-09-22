package com.samstarling.prometheusfinagle.examples

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future

class EchoService extends Service[Request, Response] {
  def apply(req: Request): Future[Response] = {
    val rep = Response(req.version, Status.Ok)
    rep.setContentString(s"Parameters: ${req.params.mkString(", ")}")
    Future(rep)
  }
}
