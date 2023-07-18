val scala3Version = "3.3.0"
val AkkaVersion = "2.8.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "learn-akka",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.12" % Test
    )
  )
