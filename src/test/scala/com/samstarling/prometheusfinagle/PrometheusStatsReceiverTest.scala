package com.samstarling.prometheusfinagle

import com.twitter.app.LoadService
import com.twitter.finagle.stats.{StatsReceiver, Verbosity}
import com.twitter.finagle.util.DefaultTimer
import com.twitter.util.Duration
import io.prometheus.client.CollectorRegistry

class PrometheusStatsReceiverTest extends UnitTest {

  "PrometheusStatsReceiverTest" should {
    "have a zero-argument constructor" in {
      new PrometheusStatsReceiver() must not(throwA[RuntimeException])
    }

    // This depends on content in test/resources/META-INF/services
    "be loaded as a StatsReceiver by LoadService" in {
      val classes: Seq[Class[_]] = LoadService[StatsReceiver]().map(_.getClass)
      classes.contains(classOf[PrometheusStatsReceiver]) ==== true
    }

    "be able to be instantiated by newInstance" in {
      classOf[PrometheusStatsReceiver].newInstance() must not(
        throwA[NoSuchMethodException])
    }

    "allow a registry to be passed" in {
      val registry = CollectorRegistry.defaultRegistry
      new PrometheusStatsReceiver(registry) must not(throwA[RuntimeException])
    }

    "allow a registry, namespace, and a Timer to be passed" in {
      val registry = CollectorRegistry.defaultRegistry
      val namespace = "testnamespace"
      new PrometheusStatsReceiver(registry,
                                  namespace,
                                  DefaultTimer.twitter,
                                  Duration.fromSeconds(1)) must not(
        throwA[RuntimeException])
    }

    "allow handle metrics and labels with unsafe characters" in {
      val registry = CollectorRegistry.defaultRegistry
      val namespace = "test_metric_names_and_labels"
      val statsReceiver = new PrometheusStatsReceiver(registry,
                                                      namespace,
                                                      DefaultTimer.twitter,
                                                      Duration.fromSeconds(1))
      val metrics = Seq(
        Seq("finagle", "build/revision"),
        Seq("foo/bar", "baz"),
        Seq("foo/bar", "build/revision"),
        Seq("foo-bar", "baz"),
        Seq("finagle", "build-revsion"),
      )
      metrics foreach { name =>
        statsReceiver.stat(Verbosity.Default, name: _*)
      } must not(throwA[IllegalArgumentException])
    }
  }
}
