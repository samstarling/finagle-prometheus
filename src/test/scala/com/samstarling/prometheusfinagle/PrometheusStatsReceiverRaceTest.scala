package com.samstarling.prometheusfinagle

import com.twitter.util.{Await, Duration, Future, FuturePool, MockTimer, Time}
import io.prometheus.client.CollectorRegistry
import java.util.concurrent.TimeUnit

import com.twitter.finagle.stats.Gauge

class PrometheusStatsReceiverRaceTest extends UnitTest {
  val threadCount = 100
  val pool = FuturePool.unboundedPool

  "PrometheusStatsReceiver#counters" should {
    "handle creating and incrementing concurrently nicely" in {
      val registry = new CollectorRegistry(true)
      val statsReceiver = new PrometheusStatsReceiver(registry).scope("test")
      val cf: Seq[Future[Unit]] = (1 to threadCount) map { _ =>
        pool {
          statsReceiver.counter("my_counter").incr(1)
        }
      }
      val joinedFutures = Future.collect(cf)

      Await.result(joinedFutures, Duration(100, TimeUnit.MILLISECONDS))
      registry.getSampleValue("finagle_my_counter", Array("serviceName"), Array("test")) === threadCount
    }

    "handle incrementing concurrently nicely" in {
      val registry = new CollectorRegistry(true)
      val statsReceiver = new PrometheusStatsReceiver(registry).scope("test")
      val counter = statsReceiver.counter("my_counter")
      val cf: Seq[Future[Unit]] = (1 to threadCount) map { n =>
        pool {
          counter.incr(1)
        }
      }
      val joinedFutures = Future.collect(cf)

      Await.result(joinedFutures, Duration(100, TimeUnit.MILLISECONDS))
      registry.getSampleValue("finagle_my_counter", Array("serviceName"), Array("test")) === threadCount
    }
  }

  "PrometheusStatsReceiver#stats" should {
    "handle creating and adding concurrently nicely" in {
      val registry = new CollectorRegistry(true)
      val statsReceiver = new PrometheusStatsReceiver(registry).scope("test")
      val cf: Seq[Future[Unit]] = (1 to threadCount) map { _ =>
        pool {
          statsReceiver.stat("my_stat").add(1.0f)
        }
      }
      val joinedFutures = Future.collect(cf)

      Await.result(joinedFutures, Duration(100, TimeUnit.MILLISECONDS))
      registry.getSampleValue("finagle_my_stat_count", Array("serviceName"), Array("test")) === threadCount
    }
  }

  "PrometheusStatsReceiver#gauges" should {
    "handle creating and adding concurrently nicely" in {
      val registry = new CollectorRegistry(true)
      val statsReceiver = new PrometheusStatsReceiver(registry).scope("test")
      val cf: Seq[Future[Gauge]] = (1 to threadCount) map { _ =>
        pool {
          statsReceiver.addGauge("my_gauge") { 123.0f }
        }
      }
      val joinedFutures = Future.collect(cf)

      Await.result(joinedFutures, Duration(100, TimeUnit.MILLISECONDS))
      registry.getSampleValue("finagle_my_gauge", Array("serviceName"), Array("test")) === 123.0f
    }

    "reflect gauge value after creation" in {
      val registry = new CollectorRegistry(true)
      val mockTimer = new MockTimer
      val statsReceiver = new PrometheusStatsReceiver(registry, "finagle", mockTimer, Duration(10, TimeUnit.SECONDS)).scope("test")

      Time.withCurrentTimeFrozen { _ =>
        var gaugeResult = 42
        statsReceiver.addGauge("my_gauge") {
          gaugeResult
        }
        registry.getSampleValue("finagle_my_gauge", Array("serviceName"), Array("test")) === 42
      }
    }

    "not reflect new gauge value before gaugePollInterval passed" in {
      val registry = new CollectorRegistry(true)
      val mockTimer = new MockTimer
      val statsReceiver = new PrometheusStatsReceiver(registry, "finagle", mockTimer, Duration(10, TimeUnit.SECONDS)).scope("test")

      Time.withCurrentTimeFrozen { timeCtl =>
        var gaugeResult = 42
        statsReceiver.addGauge("my_gauge") {
          gaugeResult
        }
        registry.getSampleValue("finagle_my_gauge", Array("serviceName"), Array("test")) === 42

        gaugeResult = 8
        timeCtl.advance(Duration(5, TimeUnit.SECONDS))
        mockTimer.tick()
        registry.getSampleValue("finagle_my_gauge", Array("serviceName"), Array("test")) === 42
      }
    }

    "reflect new gauge value after gaugePollInterval" in {
      val registry = new CollectorRegistry(true)
      val mockTimer = new MockTimer
      val statsReceiver = new PrometheusStatsReceiver(registry, "finagle", mockTimer, Duration(10, TimeUnit.SECONDS)).scope("test")

      Time.withCurrentTimeFrozen { timeCtl =>
        var gaugeResult = 42
        statsReceiver.addGauge("my_gauge") {
          gaugeResult
        }
        registry.getSampleValue("finagle_my_gauge", Array("serviceName"), Array("test")) === 42

        gaugeResult = 8

        timeCtl.advance(Duration(10, TimeUnit.SECONDS))
        mockTimer.tick()
        registry.getSampleValue("finagle_my_gauge", Array("serviceName"), Array("test")) === 8
      }
    }
  }
}
