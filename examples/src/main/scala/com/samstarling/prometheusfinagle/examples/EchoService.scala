package com.samstarling.prometheusfinagle.examples

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future

case class EchoService(message: String) extends Service[Request, Response] {
  def apply(req: Request): Future[Response] = {
    val rep = Response(req.version, Status.Ok)
    rep.setContentString(
      s"$message, the ID parameter was: ${req.params.get("id")}")
    Future(rep)
  }
}
