// val scala3Version = "3.0.0-M2"
val scala213Version = "2.13.5"

lazy val catsVersion          = "2.4.2"
lazy val catsEffectVersion    = "2.3.1"
lazy val circeVersion         = "0.14.0-M1"
lazy val declineVersion       = "1.3.0"
lazy val fs2Version           = "2.4.4"
lazy val jenaVersion          = "3.16.0"
lazy val munitVersion         = "0.7.19"
lazy val munitEffectsVersion  = "0.11.0"
lazy val poiVersion           = "4.1.2"
lazy val srdfVersion          = "0.1.89"
lazy val shexsVersion         = "0.1.81"
lazy val stringDistanceVersion = "1.2.3"

lazy val catsCore           = "org.typelevel"   %% "cats-core"            % catsVersion
lazy val catsEffect         = "org.typelevel"   %% "cats-effect"          % catsEffectVersion
lazy val circeCore          = "io.circe"                   %% "circe-core"          % circeVersion
lazy val circeGeneric       = "io.circe"                   %% "circe-generic"       % circeVersion
lazy val circeParser        = "io.circe"                   %% "circe-parser"        % circeVersion
lazy val decline            = "com.monovore"    %% "decline"              % declineVersion 
lazy val declineEffect      = "com.monovore"    %% "decline-effect"       % declineVersion 
lazy val fs2                = "co.fs2"            %% "fs2-core" % fs2Version
lazy val fs2io              = "co.fs2"            %% "fs2-io" % fs2Version
lazy val jenaArq            = "org.apache.jena" %  "jena-arq"             % jenaVersion
lazy val munit              = "org.scalameta"   %% "munit"                % munitVersion % Test
lazy val munitEffects       = "org.typelevel"   %% "munit-cats-effect-2"  % munitEffectsVersion % "test"
lazy val poi                = "org.apache.poi"  % "poi"                   % poiVersion
lazy val poi_ooxml          = "org.apache.poi"  % "poi-ooxml"             % poiVersion
lazy val srdf               = "es.weso"         %% "srdf"                 % srdfVersion
lazy val srdfJena           = "es.weso"         %% "srdfjena"             % srdfVersion
lazy val shexs              = "es.weso"         %% "shex"                 % shexsVersion

lazy val stringDistance = "com.github.vickumar1981" %% "stringdistance" % stringDistanceVersion

lazy val root = project
  .in(file("."))
  .settings(
    name := "rules2shex",
    version := "0.0.1",

    scalaVersion := scala213Version,

    libraryDependencies ++= Seq(
      catsCore, 
      catsEffect,
      circeCore, circeGeneric, circeParser,
      decline, declineEffect,
      fs2, fs2io,
      jenaArq,
      munit, 
      munitEffects,
      poi, 
      poi_ooxml,
      srdf, 
      srdfJena,
      shexs,
      stringDistance
    ), 
   testFrameworks += new TestFramework("munit.Framework"),
   useScala3doc := true, 
   resolvers ++= Seq(Resolver.bintrayRepo("labra", "maven"))
  )

