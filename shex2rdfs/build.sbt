val scala3Version = "3.0.1"

lazy val srdfVersion          = "0.1.102" 
lazy val shexsVersion         = "0.1.93"
lazy val declineVersion       = "2.1.0"
lazy val munitVersion         = "0.7.27"
lazy val munitEffectsVersion  = "1.0.4"

lazy val decline            = "com.monovore"    %% "decline"              % declineVersion 
lazy val declineEffect      = "com.monovore"    %% "decline-effect"       % declineVersion 
lazy val munit              = "org.scalameta"   %% "munit"                % munitVersion % Test
lazy val munitEffects       = "org.typelevel"   %% "munit-cats-effect-2"  % munitEffectsVersion % "test"
lazy val shexs              = "es.weso"         %% "shex"                 % shexsVersion
lazy val srdfJena           = "es.weso"         %% "srdfjena"             % srdfVersion

lazy val root = project
  .in(file("."))
  .settings(
    name := "shex2rdfs",
    version := "0.0.1",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      shexs,
      srdfJena, 
      decline,
      declineEffect
    ), 
   testFrameworks += new TestFramework("munit.Framework"),
  )

