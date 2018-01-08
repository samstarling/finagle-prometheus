# finagle-prometheus

[![Build Status](https://travis-ci.org/samstarling/finagle-prometheus.svg?branch=master)](https://travis-ci.org/samstarling/finagle-prometheus) [![Coverage Status](https://coveralls.io/repos/github/samstarling/finagle-prometheus/badge.svg?branch=master)](https://coveralls.io/github/samstarling/finagle-prometheus?branch=master)

`finagle-prometheus` is a library that provides a bridge between Finagle and Prometheus. It contains everything you need to get metrics from a Finagle application into Prometheus.

## Getting started

The library is available in Maven Central, for both Scala 2.11 and 2.12, so just add the following dependency:

```
"com.samstarling" %% "finagle-prometheus" % "0.0.4"
```

## Usage

This section will explain the three main parts of `finagle-prometheus`. You can see them all working together in the [TestServer](examples/src/main/scala/com/samstarling/prometheusfinagle/examples/TestServer.scala) in the examples directory.

### MetricsService

The `MetricsService` class is the most important one. It exposes metrics over HTTP in the format that Prometheus expects when collecting metrics. All it needs is a `CollectorRegistry`:

```scala
val registry = CollectorRegistry.defaultRegistry
val metricsService = new MetricsService(registry)
```

You can then mount this service alongside your other HTTP handers, and view metrics.

### StatsReceiver

Finagle exposes a lot of metrics internally. They are reported to a default `StatsReceiver`. You can replace this with the `PrometheusStatsReceiver`, which will transform the Finagle metrics into Prometheus metrics, and register them with the given `CollectorRegistry`.

Should you need to create one manually:

```scala
val registry = CollectorRegistry.defaultRegistry
val statsReceiver = new PrometheusStatsReceiver(registry)
```

To make Finagle use this by default, you'll need to have a file at `resources/META-INF/services/com.twitter.finagle.stats.StatsReceiver` that contains just `com.samstarling.prometheusfinagle.PrometheusStatsReceiver`. The default constructor will be used. This behaviour is documented in [Finagle's Resolver class](https://twitter.github.io/finagle/docs/com/twitter/finagle/Resolver.html), as well as in [the util-stats documentation](https://twitter.github.io/util/guide/util-stats/user_guide.html).

For full documentation of the metrics exposed, and what they mean, see [the Metrics page on the Finagle documentation site](https://twitter.github.io/finagle/guide/Metrics.html).

### Filters

The filters are much less generic. At the moment, they only relate to HTTP, and they expose entirely new metrics. The following filters exist:

* `HttpMonitoringFilter`: gives metrics relating to the number of HTTP responses, including status code and HTTP method.
* `HttpLatencyMonitoringFilter`: gives metrics relating to the latency of HTTP  responses, including status code and HTTP method.

They can only be used in conjunction with classes that conform to the type `Service[http.Request, http.Response]`. No examples exist in the `examples` directory at present.
