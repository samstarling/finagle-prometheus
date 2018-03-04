# finagle-prometheus

[![Build Status](https://travis-ci.org/samstarling/finagle-prometheus.svg?branch=master)](https://travis-ci.org/samstarling/finagle-prometheus) [![Coverage Status](https://coveralls.io/repos/github/samstarling/finagle-prometheus/badge.svg?branch=master)](https://coveralls.io/github/samstarling/finagle-prometheus?branch=master)

`finagle-prometheus` is a library that provides a bridge between Finagle and Prometheus. It contains everything you need to get metrics from a Finagle application into Prometheus.

## Getting started

The library is available in Maven Central, for both Scala 2.11 and 2.12, so just add the following dependency:

```
"com.samstarling" %% "finagle-prometheus" % "0.0.4"
```

## Custom metrics

To expose your own metrics (with labels), use the `Telemetry` class. You can see this in action in the `CustomTelemetryService` example.

```
val registry = CollectorRegistry.defaultRegistry
val telemetry = new Telemetry(registry, "namespace")

val counter = telemetry.counter(
    "requests_by_day_of_week",
    "Help text",
    Seq("day_of_week"))

counter.labels(dayOfWeek).inc()
```

## Existing Finagle metrics

### MetricsService

The `MetricsService` exposes all metrics over HTTP in the format that Prometheus expects when collecting metrics. All it needs is a `CollectorRegistry`:

```scala
val registry = CollectorRegistry.defaultRegistry
val metricsService = new MetricsService(registry)
```

You can then mount this service alongside your other HTTP handlers, and view metrics.

### StatsReceiver

A `StatsReceiver` does what it says on the tin. They are used in various places throughout Finagle,

Finagle exposes a lot of metrics internally. They are reported to a default `StatsReceiver`. You can replace this with the `PrometheusStatsReceiver`, which will transform the Finagle metrics into Prometheus metrics, and register them with the given `CollectorRegistry`.

Should you need to create one manually:

```scala
val registry = CollectorRegistry.defaultRegistry
val statsReceiver = new PrometheusStatsReceiver(registry)
```

Perhaps the best thing you can do is use the `PrometheusStatsReceiver` by default. To do this, create a file at `resources/META-INF/services/com.twitter.finagle.stats.StatsReceiver`. The contents should be:

```
com.samstarling.prometheusfinagle.PrometheusStatsReceiver
```

The default constructor for the class will be used. This behaviour is documented in [Finagle's Resolver class](https://twitter.github.io/finagle/docs/com/twitter/finagle/Resolver.html), as well as in [the util-stats documentation](https://twitter.github.io/util/guide/util-stats/user_guide.html). For full documentation of the metrics exposed, and what they mean, see [the Metrics page on the Finagle documentation site](https://twitter.github.io/finagle/guide/Metrics.html).

## Filters

The filters are much less generic. At the moment, they only relate to HTTP, and they expose entirely new metrics. The following filters exist:

* `HttpMonitoringFilter`: gives metrics relating to the number of HTTP responses, including status code and HTTP method.
* `HttpLatencyMonitoringFilter`: gives metrics relating to the latency of HTTP  responses, including status code and HTTP method.

They can only be used in conjunction with classes that conform to the type `Service[http.Request, http.Response]`. No examples exist in the `examples` directory at present.
