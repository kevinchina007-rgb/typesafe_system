ThisBuild / scalaVersion := "3.3.3"
ThisBuild / organization := "com.typesafe.travel"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val backendSourceRoot = file("src/main/scala")
lazy val backendTestRoot = file("src/test")
lazy val backendApiTestRoot = file("src/api-test")

lazy val microserviceSourceDirs: Map[String, String] = Map(
  "advertising-domain" -> "microservices/advertising",
  "attraction-domain" -> "microservices/attraction",
  "auth-domain" -> "microservices/auth",
  "content-domain" -> "microservices/content",
  "flight-domain" -> "microservices/flight",
  "hotel-domain" -> "microservices/hotel",
  "identity-domain" -> "microservices/identity",
  "inventory-domain" -> "microservices/inventory",
  "operations-domain" -> "microservices/operations",
  "order-domain" -> "microservices/order",
  "planner-service" -> "microservices/planner",
  "search-service" -> "microservices/search",
  "tour-group-domain" -> "microservices/tour-group",
  "train-domain" -> "microservices/train",
  "traveler-domain" -> "microservices/traveler"
)

def moduleSourceDir(moduleName: String) =
  backendSourceRoot / microserviceSourceDirs.getOrElse(moduleName, moduleName)

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
    searchService,
    advertisingDomain,
    plannerService,
    authDomain,
    identityDomain,
    travelerDomain,
    flightDomain,
    hotelDomain,
    trainDomain,
    attractionDomain,
    contentDomain,
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
  .settings(
    libraryDependencies ++= Seq(munitDependency)
  )

lazy val searchService = module("search-service")
  .dependsOn(sharedKernel)
  .settings(
    Compile / unmanagedSourceDirectories += moduleSourceDir("search-service") / "api",
    libraryDependencies ++= Seq(munitDependency)
  )

  lazy val advertisingDomain = module("advertising-domain")
    .dependsOn(sharedKernel)
    .settings(
      libraryDependencies ++= Seq(catsCoreDependency, catsEffectDependency, circeGenericDependency, circeParserDependency, munitDependency)
    )

lazy val plannerService = module("planner-service")
  .dependsOn(sharedKernel)
  .settings(
    Compile / unmanagedSourceDirectories += moduleSourceDir("planner-service") / "api",
    libraryDependencies ++= Seq(catsCoreDependency, circeGenericDependency, circeParserDependency, munitDependency)
  )

  lazy val authDomain = module("auth-domain")
    .dependsOn(sharedKernel, identityDomain, operationsDomain, trainDomain)
    .settings(
      libraryDependencies ++= Seq(catsCoreDependency, catsEffectDependency, circeGenericDependency, circeParserDependency, munitDependency)
    )

lazy val identityDomain = module("identity-domain")
  .dependsOn(sharedKernel)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, circeGenericDependency, circeParserDependency, munitDependency)
  )

lazy val travelerDomain = module("traveler-domain")
  .dependsOn(sharedKernel, identityDomain)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, circeGenericDependency, circeParserDependency, munitDependency)
  )

lazy val flightDomain = module("flight-domain")
  .dependsOn(sharedKernel)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, circeGenericDependency, circeParserDependency, munitDependency)
  )

lazy val hotelDomain = module("hotel-domain")
  .dependsOn(sharedKernel)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, circeGenericDependency, circeParserDependency, munitDependency)
  )

lazy val trainDomain = module("train-domain")
  .dependsOn(sharedKernel)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, circeGenericDependency, circeParserDependency, munitDependency)
  )

lazy val attractionDomain = module("attraction-domain")
  .dependsOn(sharedKernel, travelerDomain)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, munitDependency, circeGenericDependency, circeParserDependency)
  )

lazy val tourGroupDomain = module("tour-group-domain")
  .dependsOn(sharedKernel, travelerDomain)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, circeGenericDependency, circeParserDependency, munitDependency)
  )

lazy val inventoryDomain = module("inventory-domain")
  .dependsOn(sharedKernel)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, circeGenericDependency, circeParserDependency, munitDependency)
  )

  lazy val orderDomain = module("order-domain")
    .dependsOn(sharedKernel, travelerDomain, trainDomain, attractionDomain)
    .settings(
      libraryDependencies ++= Seq(catsCoreDependency, circeGenericDependency, circeParserDependency, munitDependency)
    )

lazy val persistenceJdbc = module("persistence-jdbc")
  .dependsOn(sharedKernel, searchService, advertisingDomain, authDomain, identityDomain, travelerDomain, flightDomain, hotelDomain, trainDomain, attractionDomain, contentDomain, tourGroupDomain, inventoryDomain, orderDomain, operationsDomain)
  .settings(
    Compile / unmanagedSourceDirectories ++= Seq(
      backendSourceRoot / "database",
      moduleSourceDir("advertising-domain") / "api",
      moduleSourceDir("auth-domain") / "api",
      moduleSourceDir("identity-domain") / "api",
      moduleSourceDir("traveler-domain") / "api",
      moduleSourceDir("flight-domain") / "api",
      moduleSourceDir("hotel-domain") / "api",
      moduleSourceDir("train-domain") / "api",
      moduleSourceDir("attraction-domain") / "api",
      moduleSourceDir("content-domain") / "api",
      moduleSourceDir("tour-group-domain") / "api",
      moduleSourceDir("inventory-domain") / "api",
      moduleSourceDir("order-domain") / "api",
      moduleSourceDir("operations-domain") / "api",
      moduleSourceDir("operations-domain") / "airline" / "api",
      moduleSourceDir("operations-domain") / "hotel" / "api",
      moduleSourceDir("operations-domain") / "attraction" / "api",
      moduleSourceDir("operations-domain") / "siteadmin" / "api",
      moduleSourceDir("advertising-domain") / "tables",
      moduleSourceDir("auth-domain") / "tables",
      moduleSourceDir("identity-domain") / "tables",
      moduleSourceDir("traveler-domain") / "tables",
      moduleSourceDir("flight-domain") / "tables",
      moduleSourceDir("hotel-domain") / "tables",
      moduleSourceDir("train-domain") / "tables",
      moduleSourceDir("attraction-domain") / "tables",
      moduleSourceDir("content-domain") / "tables",
      moduleSourceDir("tour-group-domain") / "tables",
      moduleSourceDir("inventory-domain") / "tables",
      moduleSourceDir("order-domain") / "tables",
      moduleSourceDir("operations-domain") / "tables",
      moduleSourceDir("operations-domain") / "airline" / "tables",
      moduleSourceDir("operations-domain") / "hotel" / "tables",
      moduleSourceDir("operations-domain") / "attraction" / "tables",
      moduleSourceDir("operations-domain") / "siteadmin" / "tables"
    ),
    Compile / unmanagedSources += backendSourceRoot / "routes" / "ApiPlan.scala",
    Compile / unmanagedResourceDirectories += backendSourceRoot / "database" / "migrations",
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
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, circeGenericDependency, circeParserDependency, munitDependency)
  )

lazy val operationsDomain = module("operations-domain")
  .dependsOn(sharedKernel)
  .settings(
    Compile / unmanagedSourceDirectories ++= Seq(
      moduleSourceDir("operations-domain") / "airline" / "objects",
      moduleSourceDir("operations-domain") / "hotel" / "objects",
      moduleSourceDir("operations-domain") / "attraction" / "objects",
      moduleSourceDir("operations-domain") / "siteadmin" / "objects"
    ),
    libraryDependencies ++= Seq(catsCoreDependency, circeGenericDependency, circeParserDependency, munitDependency)
  )

lazy val apiGateway = Project(id = "api-gateway", base = file("projects/api-gateway"))
  .settings(commonSettings)
  .dependsOn(
    sharedKernel,
    searchService,
    advertisingDomain,
    authDomain,
    identityDomain,
      travelerDomain,
      flightDomain,
      hotelDomain,
      trainDomain,
      attractionDomain,
      contentDomain,
      tourGroupDomain,
      inventoryDomain,
      orderDomain,
      operationsDomain,
    persistenceJdbc
  )
  .settings(
    Compile / unmanagedSourceDirectories ++= Seq(
      backendSourceRoot / "app",
      backendSourceRoot / "static",
      backendSourceRoot / "routes",
      moduleSourceDir("advertising-domain") / "api",
      moduleSourceDir("auth-domain") / "api",
      moduleSourceDir("identity-domain") / "api",
      moduleSourceDir("traveler-domain") / "api",
      moduleSourceDir("flight-domain") / "api",
      moduleSourceDir("hotel-domain") / "api",
      moduleSourceDir("train-domain") / "api",
      moduleSourceDir("attraction-domain") / "api",
      moduleSourceDir("content-domain") / "api",
      moduleSourceDir("tour-group-domain") / "api",
      moduleSourceDir("inventory-domain") / "api",
      moduleSourceDir("order-domain") / "api",
      moduleSourceDir("operations-domain") / "api",
      moduleSourceDir("operations-domain") / "airline" / "api",
      moduleSourceDir("operations-domain") / "hotel" / "api",
      moduleSourceDir("operations-domain") / "attraction" / "api",
      moduleSourceDir("operations-domain") / "siteadmin" / "api"
    ),
    Test / unmanagedSourceDirectories ++= Seq(
      backendApiTestRoot / "identity-domain",
      backendApiTestRoot / "traveler-domain",
      backendApiTestRoot / "flight-domain",
      backendApiTestRoot / "hotel-domain",
      backendApiTestRoot / "train-domain",
      backendApiTestRoot / "attraction-domain",
      backendApiTestRoot / "content-domain",
      backendApiTestRoot / "tour-group-domain",
      backendApiTestRoot / "inventory-domain",
      backendApiTestRoot / "order-domain",
      backendApiTestRoot / "operations-domain",
      backendApiTestRoot / "shared-kernel"
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
  Project(id = moduleName, base = file(s"projects/$moduleName"))
    .settings(commonSettings)
    .settings(
      name := moduleName,
      Compile / unmanagedSourceDirectories ++= Seq(
        moduleSourceDir(moduleName) / "objects"
      ),
      Test / unmanagedSourceDirectories += backendTestRoot / moduleName
    )
