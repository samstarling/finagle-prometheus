package com.samstarling.service

import java.io.StringWriter

import com.twitter.common.metrics.Metrics
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import io.prometheus.client.Collector
import io.prometheus.client.Collector.MetricFamilySamples
import io.prometheus.client.Collector.MetricFamilySamples.Sample
import io.prometheus.client.exporter.common.TextFormat
import scala.collection.JavaConverters._

class PrometheusExporter(registry: Metrics) extends Service[Request, Response] {

  override def apply(request: Request): Future[Response] = {

    val gauges = registry.sampleGauges().asScala
    val histograms = registry.sampleHistograms().asScala
    val counters = registry.sampleCounters().asScala

    val gaugeFamilySamples = gauges.map { case (metricName, metricValue) =>
      val sample = new Sample(sanitizeName(metricName), List.empty.asJava, List.empty.asJava, metricValue.doubleValue())
      new MetricFamilySamples(sanitizeName(metricName), Collector.Type.GAUGE, "No help", List(sample).asJava)
    }

    val histogramFamilySamples = histograms.map { case (metricName, metricValue) =>
      val samples = metricValue.percentiles().map { percentile =>
        new Sample(
          sanitizeName(metricName),
          List("quantile").asJava,
          List("p" + (percentile.getQuantile * 100).toString).asJava,
          percentile.getValue.toDouble
        )
      }.toList
      new MetricFamilySamples(sanitizeName(metricName), Collector.Type.GAUGE, "No help", samples.asJava)
    }

    val counterFamilySamples = counters.map { case (metricName, metricValue) =>
      val sample = new Sample(sanitizeName(metricName), List.empty.asJava, List.empty.asJava, metricValue.doubleValue())
      new MetricFamilySamples(sanitizeName(metricName), Collector.Type.COUNTER, "No help", List(sample).asJava)
    }

    val allSamples = gaugeFamilySamples ++ histogramFamilySamples ++ counterFamilySamples

    val writer = new StringWriter
    TextFormat.write004(writer, allSamples.iterator.asJavaEnumeration)
    val response = Response(request.version, Status.Ok)
    response.setContentString(writer.toString)
    Future(response)
  }

  private def sanitizeName(name: String) = {
    name.replace("/", "__").replace(".", "_")
  }
}
