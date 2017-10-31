package com.samstarling.prometheusfinagle

class MetricLabeller {
  def labelNamesFor(name: Seq[String]): Seq[String] = name match {
    case Seq(_, "requests")           => Seq("serviceName")
    case Seq(_, "success")            => Seq("serviceName")
    case Seq(_, "request_latency_ms") => Seq("serviceName")
    case Seq(_, "pending")            => Seq("serviceName")
    case Seq(_, "http", "time", _)    => Seq("serviceName", "statusCode")
    case Seq(_, "http", "status", _)  => Seq("serviceName", "statusCode")
    case default                      => Seq.empty
  }

  def labelsFor(name: Seq[String]): Seq[String] = name match {
    case Seq(serviceName, "requests")           => Seq(serviceName)
    case Seq(serviceName, "success")            => Seq(serviceName)
    case Seq(serviceName, "request_latency_ms") => Seq(serviceName)
    case Seq(serviceName, "pending")            => Seq(serviceName)
    case Seq(serviceName, "http", "time", statusCode) =>
      Seq(serviceName, statusCode)
    case Seq(serviceName, "http", "status", statusCode) =>
      Seq(serviceName, statusCode)
    case default => Seq.empty
  }

  def sanitizeName(name: Seq[String]): String = {
    name match {
      case Seq(_, "requests")           => "requests"
      case Seq(_, "success")            => "success"
      case Seq(_, "request_latency_ms") => "request_latency_ms"
      case Seq(_, "pending")            => "pending"
      case Seq(_, "http", "time", _)    => "http_time"
      case Seq(_, "http", "status", _)  => "http_status"
      case default                      => name.map(_.replaceAll("[^\\w]", "_")).mkString("__")
    }
  }
}
