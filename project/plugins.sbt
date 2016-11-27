addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt"    % "0.5.1")
addSbtPlugin("com.earldouglas"  % "xsbt-web-plugin" % "2.2.0")
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
    Resolver.ivyStylePatterns)


