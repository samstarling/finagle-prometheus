package com.samstarling.prometheusfinagle.examples

import java.text.SimpleDateFormat
import java.util.Calendar

import com.samstarling.prometheusfinagle.metrics.Telemetry
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future

class CustomTelemetryService(telemetry: Telemetry)
    extends Service[Request, Response] {

  private val dayOfWeekFormat = new SimpleDateFormat("E")

  private val counter = telemetry.counter("requests_by_day_of_week",
                                          "Help text",
                                          Seq("day_of_week"))

  override def apply(request: Request): Future[Response] = {
    dayOfWeek
    counter.labels(dayOfWeek).inc()
    val rep = Response(request.version, Status.Ok)
    rep.setContentString("Your request was logged!")
    Future(rep)
  }

  private def dayOfWeek: String = {
    dayOfWeekFormat.format(Calendar.getInstance.getTime)
  }
}
