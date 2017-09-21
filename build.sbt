import org.scoverage.coveralls.Imports.CoverallsKeys._

name := "finagle-prometheus"

lazy val commonSettings = Seq(
  organization := "com.samstarling",
  scalaVersion := "2.11.8",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
)

organization := "com.samstarling"

libraryDependencies ++= Seq(
  "com.twitter" % "finagle-core_2.11" % "6.40.0",
  "com.twitter" % "finagle-http_2.11" % "6.40.0",
  "com.twitter" % "finagle-stats_2.11" % "6.40.0",
  "io.prometheus" % "simpleclient" % "0.0.19",
  "io.prometheus" % "simpleclient_common" % "0.0.19",
  "org.specs2" %% "specs2-core" % "3.8.5" % "test",
  "org.specs2" %% "specs2-mock" % "3.8.5" % "test"
)

lazy val core = (project in file(".")).
  settings(commonSettings: _*).
  settings(coverageEnabled := true, coverallsTokenFile := Some("src/test/resources/coveralls-key.txt"))

lazy val examples = (project in file("examples")).
  settings(commonSettings: _*).
  dependsOn(core).
  settings()

lazy val root = project.aggregate(core, examples)

homepage := Some(url("https://github.com/samstarling/finagle-prometheus"))

pomExtra :=
  <scm>
    <connection>
      scm:git:git://github.com/samstarling/finagle-prometheus.git
    </connection>
    <url>
      https://github.com/samstarling/finagle-prometheus
    </url>
  </scm>
  <developers>
    <developer>
      <id>samstarling</id>
      <name>Sam Starling</name>
      <email>mail@samstarling.co.uk</email>
    </developer>
  </developers>
