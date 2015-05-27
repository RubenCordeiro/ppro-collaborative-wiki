import spray.revolver.RevolverPlugin.Revolver

name := "RealTimeWeaki"

version := "0.1"

scalaVersion := "2.11.6"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")

libraryDependencies ++= {
  val sprayVersion = "1.3.3"
  val akkaVersion = "2.3.11"
  Seq(
    // Spray
    "io.spray" %% "spray-can" % sprayVersion,
    "io.spray" %% "spray-routing" % sprayVersion,
    "io.spray" %% "spray-json" % "1.3.2",
    
    // Akka
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    
    // Tests
    "org.specs2" %% "specs2-core" % "2.3.11" % "test",
    "org.scalaz" %%  "scalaz-core"   % "7.1.2",

    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "io.spray" %% "spray-testkit" % sprayVersion % "test",

    "org.scala-lang.modules" %% "scala-async" % "0.9.3",

    "com.typesafe" % "config" % "1.3.0",

    "org.scala-lang" % "scala-reflect" % "2.11.6",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.4"
  )
}

resolvers += "Spray repo" at "http://repo.spray.io"

unmanagedResourceDirectories in Compile += baseDirectory.value / "CollabWeaki"

excludeFilter in Compile in unmanagedResources := "*.iml"

val npmTask = TaskKey[Unit]("npmTask", "Runs npm.")

npmTask := {
  println("Running Task")
  println(Process("npm.cmd install", baseDirectory.value / "CollabWeaki")!!)
}

// compile in Compile <<= (compile in Compile) dependsOn npmTask

Revolver.settings