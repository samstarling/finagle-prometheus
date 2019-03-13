package com.samstarling.prometheusfinagle.filter

import com.twitter.finagle.http.{Request, Response}

/**
  * ServiceLabeller allows Finagle metrics to be labelled
  * specific to the request and response.
  *
  * @tparam Req the type of the request to label
  * @tparam Rep the type of the response to label
  */
trait ServiceLabeller[Req, Rep] {
  /**
    * The keys that metrics should be labelled with.
    * @return a sequence of key names
    */
  def keys: Seq[String]

  /**
    * For a given request and response, this method will choose a value for each key.
    * @param request the incoming request to label metrics for
    * @param response the resulting response to label metrics for
    * @return a sequence of string values, corresponding to [[keys]]
    */
  def labelsFor(request: Req, response: Rep): Seq[String]
}

class HttpServiceLabeller extends ServiceLabeller[Request, Response] {

  def keys: Seq[String] = Seq("status", "statusClass", "method")

  def labelsFor(request: Request, response: Response): List[String] = List(
    response.status.code.toString,
    s"${response.status.code.toString.charAt(0)}xx",
    request.method.toString
  )
}
