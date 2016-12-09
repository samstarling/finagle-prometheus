package com.samstarling.filter

import com.twitter.finagle.http.Status

protected object StatusClass {
  def forStatus(status: Status): String = {
    status.code.toString.charAt(0) + "xx"
  }
}
