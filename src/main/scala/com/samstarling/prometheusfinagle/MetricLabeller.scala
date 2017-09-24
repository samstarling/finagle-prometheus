package com.samstarling.prometheusfinagle

class MetricLabeller {
  def labelNamesFor(name: Seq[String]): (String, Seq[String]) = name match {
    case Seq(_, "requests") => ("requests", Seq("serviceName"))
    case Seq(_, "success") => ("success", Seq("serviceName"))
    case Seq(_, "request_latency_ms") => ("request_latency_ms", Seq("serviceName"))
    case Seq(_, "pending") => ("pending", Seq("serviceName"))
    case default => (sanitizeName(default), Seq.empty)
  }

  def labelsFor(name: Seq[String]): Seq[String] = name match {
    case Seq(label, "requests") => Seq(label)
    case Seq(label, "success") => Seq(label)
    case Seq(label, "request_latency_ms") => Seq(label)
    case Seq(label, "pending") => Seq(label)

    case default => Seq.empty
  }

  private def sanitizeName(name: Seq[String]): String = {
    name.map(_.replaceAll("[^\\w]", "_")).mkString("__")
  }
}
