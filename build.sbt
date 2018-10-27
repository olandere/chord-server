import com.earldouglas.xwp.JettyPlugin
import org.scalatra.sbt._

lazy val root = (project in file(".")).
  enablePlugins(JettyPlugin).
  settings(
    organization := "com.example",
    name := "chord-server",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.12.7",


    resolvers ++= Seq(Classpaths.typesafeReleases, Resolver.jcenterRepo),
    excludeFilter := HiddenFileFilter -- ".ebextensions",
    //  ideaExcludeFolders := Seq(".idea",".idea_modules"),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
      "org.scalatra" %% "scalatra" % "2.5.1",
      "org.scalatra" %% "scalatra-scalate" % "2.5.1",
      "org.scalatra" %% "scalatra-specs2" % "2.5.1" % "test",
      "org.scalatra" %% "scalatra-scalatest" % "2.5.1" % "test",
      "org.eclipse.jetty" % "jetty-webapp" % "9.4.12.v20180830" % "container",
      "org.eclipse.jetty" % "jetty-plus" % "9.4.12.v20180830" % "container",
      "javax.servlet" % "javax.servlet-api" % "3.1.0",
      "org.scalatra" %% "scalatra-json" % "2.5.1",
      "org.json4s" %% "json4s-native" % "3.6.1",
      "org.clapper" %% "grizzled-slf4j" % "1.3.2",
      "chords" %% "chords" % "1.0",
      "org.typelevel" %% "cats-core" % "1.4.0",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.1"
    )
  ).settings(ScalatraPlugin.scalatraSettings)
