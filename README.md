# finagle-prometheus

[![Build Status](https://travis-ci.org/samstarling/finagle-prometheus.svg?branch=master)](https://travis-ci.org/samstarling/finagle-prometheus) [![Coverage Status](https://coveralls.io/repos/github/samstarling/finagle-prometheus/badge.svg?branch=master)](https://coveralls.io/github/samstarling/finagle-prometheus?branch=master)

A library that provides a bridge between Finagle and Prometheus. It contains everything you need to get metrics from a Finagle service into Prometheus.

The library is [published on Bintray](https://bintray.com/samstarling/maven/finagle-prometheus). To include it as a dependency, add the following lines:

```
resolvers += Resolver.bintrayRepo("samstarling", "maven")
"com.samstarling" %% "finagle-prometheus" % "0.0.1"
```
