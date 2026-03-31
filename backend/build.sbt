ThisBuild / scalaVersion := "3.3.3"
ThisBuild / organization := "com.typesafe.travel"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
)

lazy val catsCoreDependency =
  "org.typelevel" %% "cats-core" % "2.12.0"

lazy val catsEffectDependency =
  "org.typelevel" %% "cats-effect" % "3.5.4"

lazy val munitDependency =
  "org.scalameta" %% "munit" % "1.0.0" % Test

lazy val http4sDslDependency =
  "org.http4s" %% "http4s-dsl" % "0.23.27"

lazy val http4sEmberServerDependency =
  "org.http4s" %% "http4s-ember-server" % "0.23.27"

lazy val http4sCirceDependency =
  "org.http4s" %% "http4s-circe" % "0.23.27"

lazy val http4sServerDependency =
  "org.http4s" %% "http4s-server" % "0.23.27"

lazy val circeGenericDependency =
  "io.circe" %% "circe-generic" % "0.14.9"

lazy val circeParserDependency =
  "io.circe" %% "circe-parser" % "0.14.9"

lazy val doobieCoreDependency =
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC5"

lazy val doobieHikariDependency =
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC5"

lazy val doobieH2Dependency =
  "org.tpolecat" %% "doobie-h2" % "1.0.0-RC5"

lazy val postgresqlDependency =
  "org.postgresql" % "postgresql" % "42.7.4"

lazy val root = (project in file("."))
  .aggregate(
    sharedKernel,
    identityDomain,
    travelerDomain,
    flightDomain,
    hotelDomain,
    trainDomain,
    attractionDomain,
    tourGroupDomain,
    inventoryDomain,
    orderDomain,
    operationsDomain,
    persistenceJdbc,
    apiGateway
  )
  .settings(
    name := "travel-platform-backend",
    publish / skip := true,
    Compile / unmanagedSourceDirectories := Seq.empty,
    Test / unmanagedSourceDirectories := Seq.empty,
    Compile / run / fork := true,
    Compile / run := (apiGateway / Compile / run).evaluated,
    Compile / run / mainClass := Some("com.typesafe.travel.api.Main")
  )

lazy val sharedKernel = module("shared-kernel")

lazy val identityDomain = module("identity-domain")
  .dependsOn(sharedKernel)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, munitDependency)
  )

lazy val travelerDomain = module("traveler-domain")
  .dependsOn(sharedKernel, identityDomain)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, munitDependency)
  )

lazy val flightDomain = module("flight-domain")
  .dependsOn(sharedKernel)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, munitDependency)
  )

lazy val hotelDomain = module("hotel-domain")
  .dependsOn(sharedKernel)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, munitDependency)
  )

lazy val trainDomain = module("train-domain")
  .dependsOn(sharedKernel)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, munitDependency)
  )

lazy val attractionDomain = module("attraction-domain")
  .dependsOn(sharedKernel, travelerDomain)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, munitDependency, circeGenericDependency, circeParserDependency)
  )

lazy val tourGroupDomain = module("tour-group-domain")
  .dependsOn(sharedKernel, travelerDomain)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, munitDependency)
  )

lazy val inventoryDomain = module("inventory-domain")
  .dependsOn(sharedKernel)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, munitDependency)
  )

lazy val orderDomain = module("order-domain")
  .dependsOn(sharedKernel, travelerDomain, trainDomain, attractionDomain)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, munitDependency)
  )

lazy val persistenceJdbc = module("persistence-jdbc")
  .dependsOn(sharedKernel, identityDomain, travelerDomain, flightDomain, hotelDomain, trainDomain, attractionDomain, tourGroupDomain, inventoryDomain, orderDomain, operationsDomain)
  .settings(
    libraryDependencies ++= Seq(
      catsEffectDependency,
      circeGenericDependency,
      circeParserDependency,
      doobieCoreDependency,
      doobieHikariDependency,
      doobieH2Dependency,
      postgresqlDependency,
      munitDependency
    )
  )

lazy val contentDomain = module("content-domain")
  .dependsOn(sharedKernel, orderDomain)

lazy val operationsDomain = module("operations-domain")
  .dependsOn(sharedKernel)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, munitDependency)
  )

lazy val apiGateway = module("api-gateway")
  .dependsOn(
    sharedKernel,
    identityDomain,
      travelerDomain,
      flightDomain,
      hotelDomain,
      trainDomain,
      attractionDomain,
      tourGroupDomain,
      inventoryDomain,
      orderDomain,
      operationsDomain,
    persistenceJdbc
  )
  .settings(
    Compile / unmanagedSourceDirectories ++= Seq(
      baseDirectory.value.getParentFile / "identity-domain" / "src" / "api",
      baseDirectory.value.getParentFile / "traveler-domain" / "src" / "api",
      baseDirectory.value.getParentFile / "flight-domain" / "src" / "api",
      baseDirectory.value.getParentFile / "hotel-domain" / "src" / "api",
      baseDirectory.value.getParentFile / "train-domain" / "src" / "api",
      baseDirectory.value.getParentFile / "attraction-domain" / "src" / "api",
      baseDirectory.value.getParentFile / "tour-group-domain" / "src" / "api",
      baseDirectory.value.getParentFile / "inventory-domain" / "src" / "api",
      baseDirectory.value.getParentFile / "order-domain" / "src" / "api",
      baseDirectory.value.getParentFile / "operations-domain" / "src" / "api",
      baseDirectory.value.getParentFile / "shared-kernel" / "src" / "api"
    ),
    Test / unmanagedSourceDirectories ++= Seq(
      baseDirectory.value.getParentFile / "identity-domain" / "src" / "api-test",
      baseDirectory.value.getParentFile / "traveler-domain" / "src" / "api-test",
      baseDirectory.value.getParentFile / "flight-domain" / "src" / "api-test",
      baseDirectory.value.getParentFile / "hotel-domain" / "src" / "api-test",
      baseDirectory.value.getParentFile / "train-domain" / "src" / "api-test",
      baseDirectory.value.getParentFile / "attraction-domain" / "src" / "api-test",
      baseDirectory.value.getParentFile / "tour-group-domain" / "src" / "api-test",
      baseDirectory.value.getParentFile / "inventory-domain" / "src" / "api-test",
      baseDirectory.value.getParentFile / "order-domain" / "src" / "api-test",
      baseDirectory.value.getParentFile / "operations-domain" / "src" / "api-test",
      baseDirectory.value.getParentFile / "shared-kernel" / "src" / "api-test"
    ),
    libraryDependencies ++= Seq(
      catsEffectDependency,
      http4sDslDependency,
      http4sEmberServerDependency,
      http4sCirceDependency,
      http4sServerDependency,
      circeGenericDependency,
      circeParserDependency,
      munitDependency
    ),
    Compile / run / fork := true,
    Compile / run / mainClass := Some("com.typesafe.travel.api.Main")
  )

def module(moduleName: String) =
  Project(id = moduleName, base = file(s"modules/$moduleName"))
    .settings(commonSettings)
    .settings(
      name := moduleName,
      Compile / unmanagedSourceDirectories += baseDirectory.value / "src" / "main",
      Test / unmanagedSourceDirectories += baseDirectory.value / "src" / "test"
    )
