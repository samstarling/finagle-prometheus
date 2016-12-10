name := "prometheus-finagle"

organization := "com.samstarling"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

scalaVersion := "2.11.8"

coverageEnabled := true

libraryDependencies ++= Seq(
  "com.twitter" % "finagle-core_2.11" % "6.40.0",
  "com.twitter" % "finagle-http_2.11" % "6.40.0",
  "com.twitter" % "finagle-stats_2.11" % "6.40.0",
  "io.prometheus" % "simpleclient" % "0.0.19",
  "io.prometheus" % "simpleclient_common" % "0.0.19",
  "org.specs2" %% "specs2-core" % "3.8.5" % "test",
  "org.specs2" %% "specs2-mock" % "3.8.5" % "test"
)
