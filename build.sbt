import com.earldouglas.xwp.JettyPlugin
import org.scalatra.sbt._

lazy val root = (project in file(".")).
  enablePlugins(JettyPlugin).
  settings(
    organization := "com.example",
    name := "chord-server",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.12.1",


    resolvers += Classpaths.typesafeReleases,
    excludeFilter := HiddenFileFilter -- ".ebextensions",
    //  ideaExcludeFolders := Seq(".idea",".idea_modules"),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.1.3" % "runtime",
      "org.scalatra" %% "scalatra" % "2.5.0",
      "org.scalatra" %% "scalatra-scalate" % "2.5.0",
      "org.scalatra" %% "scalatra-specs2" % "2.5.0" % "test",
      "org.scalatra" %% "scalatra-scalatest" % "2.5.0" % "test",
      "org.eclipse.jetty" % "jetty-webapp" % "9.1.5.v20140505" % "container",
      "org.eclipse.jetty" % "jetty-plus" % "9.1.5.v20140505" % "container",
      "javax.servlet" % "javax.servlet-api" % "3.1.0",
      "org.scalatra" %% "scalatra-json" % "2.5.0",
      "org.json4s" %% "json4s-jackson" % "3.5.0",
      "org.clapper" %% "grizzled-slf4j" % "1.3.0",
      "chords" %% "chords" % "1.0",
      "org.typelevel" %% "cats" % "0.8.1",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
    )
  ).settings(ScalatraPlugin.scalatraSettings)
