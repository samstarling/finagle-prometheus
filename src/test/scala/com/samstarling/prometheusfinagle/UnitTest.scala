package com.samstarling.prometheusfinagle

import com.samstarling.prometheusfinagle.filter.RequestLabeller
import com.twitter.finagle.http.{Request, Response}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

trait UnitTest extends Specification with Mockito {

  class TestLabeller extends RequestLabeller {
    override def keys: List[String] = List("foo")
    override def labelsFor(request: Request, response: Response): List[String] =
      List("bar")
  }

}
