package com.samstarling.prometheusfinagle

import org.specs2.mutable.Specification

class DefaultMetricPatternsTest extends UnitTest {

  "DefaultMetricPatternsTest" should {
    "handle all metrics with the All pattern" in {
      val finagleMetrics = Set(
        Seq("api.github.com:/emojis:GET", "request_payload_bytes"),
        Seq("api.github.com:/emojis:GET", "response_payload_bytes"),
        Seq("api.github.com:/emojis:GET", "http", "response_size"),
        Seq("api.github.com:/emojis:GET", "loadbalancer", "meanweight"),
        Seq("api.github.com:/emojis:GET", "loadbalancer", "available"),
        Seq("api.github.com:/emojis:GET", "loadbalancer", "busy"),
        Seq("api.github.com:/emojis:GET", "loadbalancer", "closed"),
        Seq("api.github.com:/emojis:GET", "loadbalancer", "load"),
        Seq("api.github.com:/emojis:GET", "loadbalancer", "size"),
        Seq("api.github.com:/emojis:GET", "loadbalancer", "adds"),
        Seq("api.github.com:/emojis:GET", "loadbalancer", "removes"),
        Seq("api.github.com:/emojis:GET", "loadbalancer", "rebuilds"),
        Seq("api.github.com:/emojis:GET", "loadbalancer", "updates"),
        Seq("api.github.com:/emojis:GET", "loadbalancer", "max_effort_exhausted"),
        Seq("api.github.com:/emojis:GET", "loadbalancer", "algorithm", "p2c_least_loaded"),
        Seq("api.github.com:/emojis:GET", "service_creation", "service_acquisition_latency_ms"),
        Seq("api.github.com:/emojis:GET", "nack_admission_control", "dropped_requests"),
        Seq("api.github.com:/emojis:GET", "retries", "requeues"),
        Seq("api.github.com:/emojis:GET", "retries", "requeues"),
        Seq("api.github.com:/emojis:GET", "retries", "budget_exhausted"),
        Seq("api.github.com:/emojis:GET", "retries", "request_limit"),
        Seq("api.github.com:/emojis:GET", "retries", "requeues_per_request"),
        Seq("api.github.com:/emojis:GET", "retries", "cannot_retry"),
        Seq("api.github.com:/emojis:GET", "retries", "budget"),
        Seq("api.github.com:/emojis:GET", "retries", "not_open"),
        Seq("api.github.com:/emojis:GET", "connection_requests"),
        Seq("api.github.com:/emojis:GET", "connects"),
        Seq("api.github.com:/emojis:GET", "connection_duration"),
        Seq("api.github.com:/emojis:GET", "connection_received_bytes"),
        Seq("api.github.com:/emojis:GET", "connection_sent_bytes"),
        Seq("api.github.com:/emojis:GET", "socket_writable_ms"),
        Seq("api.github.com:/emojis:GET", "socket_unwritable_ms"),
        Seq("api.github.com:/emojis:GET", "received_bytes"),
        Seq("api.github.com:/emojis:GET", "sent_bytes"),
        Seq("api.github.com:/emojis:GET", "closes"),
        Seq("api.github.com:/emojis:GET", "connections"),
        Seq("api.github.com:/emojis:GET", "read_timeout"),
        Seq("api.github.com:/emojis:GET", "write_timeout"),
        Seq("api.github.com:/emojis:GET", "connect_latency_ms"),
        Seq("api.github.com:/emojis:GET", "failed_connect_latency_ms"),
        Seq("api.github.com:/emojis:GET", "cancelled_connects"),
        Seq("api.github.com:/emojis:GET", "failfast", "marked_available"),
        Seq("api.github.com:/emojis:GET", "failfast", "marked_dead"),
        Seq("api.github.com:/emojis:GET", "failfast", "unhealthy_for_ms"),
        Seq("api.github.com:/emojis:GET", "failfast", "unhealthy_num_tries"),
        Seq("api.github.com:/emojis:GET", "pool_cached"),
        Seq("api.github.com:/emojis:GET", "pool_num_waited"),
        Seq("api.github.com:/emojis:GET", "pool_num_too_many_waiters"),
        Seq("api.github.com:/emojis:GET", "pool_waiters"),
        Seq("api.github.com:/emojis:GET", "pool_size"),
        Seq("api.github.com:/emojis:GET", "failure_accrual", "removals"),
        Seq("api.github.com:/emojis:GET", "failure_accrual", "revivals"),
        Seq("api.github.com:/emojis:GET", "failure_accrual", "probes"),
        Seq("api.github.com:/emojis:GET", "failure_accrual", "removed_for_ms"),
        Seq("api.github.com:/emojis:GET", "available"),
        Seq("api.github.com:/emojis:GET", "requests"),
        Seq("api.github.com:/emojis:GET", "success"),
        Seq("api.github.com:/emojis:GET", "request_latency_ms"),
        Seq("api.github.com:/emojis:GET", "pending"),
        Seq("api.github.com:/emojis:GET", "dtab", "size"),
        Seq("api.github.com:/emojis:GET", "dispatcher", "serial", "queue_size"),
        Seq("api.github.com:/emojis:GET", "http", "status", "200"),
        Seq("api.github.com:/emojis:GET", "http", "status", "2XX"),
        Seq("api.github.com:/emojis:GET", "http", "time", "200"),
        Seq("api.github.com:/emojis:GET", "http", "time", "2XX")
      )
      println(finagleMetrics.filterNot(DefaultMetricPatterns.All.isDefinedAt))

      (DefaultMetricPatterns.All.isDefinedAt(_:Seq[String]) must beTrue).foreach(finagleMetrics)
    }

  }
}
