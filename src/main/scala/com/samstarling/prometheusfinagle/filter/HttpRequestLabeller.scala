package com.samstarling.prometheusfinagle.filter

import com.twitter.finagle.http.{Request, Response}

trait RequestLabeller {

  /**
    * TODO Explain why this is modelled like this
    * @return
    */
  def keys: Seq[String]

  /**
    * TODO Explain why this is modelled like this
    * @return
    */
  def labelsFor(request: Request, response: Response): Seq[String]
}

// TODO Include response in name
class HttpRequestLabeller extends RequestLabeller {

  def keys: Seq[String] = Seq(
    "status",
    "statusClass",
    "method"
  )

  def labelsFor(request: Request, response: Response): List[String] = List(
    response.status.code.toString,
    s"${response.status.code.toString.charAt(0)}xx",
    request.method.toString
  )
}
