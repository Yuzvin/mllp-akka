lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion     = "2.5.22"
lazy val slickVersion    = "3.2.0"
lazy val macwireVersion  = "2.3.2"

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "com.yuzvin",
      scalaVersion := "2.12.7"
    )),
  name := "MLLP-Akka",
  libraryDependencies ++= Seq(
    "com.typesafe.akka"          %% "akka-http"            % akkaHttpVersion,
    "com.typesafe.akka"          %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka"          %% "akka-http-xml"        % akkaHttpVersion,
    "com.typesafe.akka"          %% "akka-stream"          % akkaVersion,
    "com.typesafe.slick"         %% "slick"                % slickVersion,
    "com.typesafe.slick"         %% "slick-hikaricp"       % slickVersion,
    "org.postgresql"             % "postgresql"            % "9.4-1206-jdbc42",
    "com.softwaremill.macwire"   %% "macros"               % macwireVersion % "provided",
    "com.softwaremill.macwire"   %% "macrosakka"           % macwireVersion % "provided",
    "com.softwaremill.macwire"   %% "util"                 % macwireVersion,
    "com.softwaremill.macwire"   %% "proxy"                % macwireVersion,
    "ch.qos.logback"             % "logback-classic"       % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging"        % "3.9.2",
    "com.typesafe.akka"          %% "akka-http-testkit"    % akkaHttpVersion % Test,
    "com.typesafe.akka"          %% "akka-testkit"         % akkaVersion % Test,
    "com.typesafe.akka"          %% "akka-stream-testkit"  % akkaVersion % Test,
    "org.scalatest"              %% "scalatest"            % "3.0.5" % Test,
    "org.mockito"                %% "mockito-scala"        % "1.3.1" % Test
  )
)
