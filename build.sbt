name := """Tasks4Squares"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "org.reactivemongo" %% "reactivemongo" % "0.11.7",
  "com.github.nscala-time" %% "nscala-time" % "2.4.0",
  "com.github.agourlay" %% "cornichon" % "0.2.2" % "test"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesGenerator := InjectedRoutesGenerator

parallelExecution in Test := false
