ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "slick-with-scala"
  )

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.4.1",
  "org.postgresql" % "postgresql" % "42.5.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
  "com.github.tminglei" %% "slick-pg" % "0.21.1",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.21.1"
)