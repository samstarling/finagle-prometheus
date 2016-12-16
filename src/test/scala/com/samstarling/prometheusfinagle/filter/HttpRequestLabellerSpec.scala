package com.samstarling.prometheusfinagle.filter

import com.samstarling.prometheusfinagle.UnitTest
import com.twitter.finagle.http.{Method, Request, Response, Status}
import org.specs2.specification.Scope

class HttpRequestLabellerSpec extends UnitTest {

  trait Context extends Scope {
    val request = Request(Method.Get, "/foo/bar")
    val response = Response(Status.Ok)
    val labeller = new HttpRequestLabeller()
    val labels = labeller.labelsFor(request, response)
  }

  "keys" >> {
    "returns the keys in the correct order" in new Context {
      labeller.keys ==== Seq("status", "statusClass", "method")
    }
  }

  "labelsFor" >> {
    "returns the status code of the response" in new Context {
      labels(0) ==== "200"
    }

    "returns the status class of the request" in new Context {
      labels(1) ==== "2xx"
    }

    "returns the method of the request" in new Context {
      labels(2) ==== "GET"
    }
  }
}
