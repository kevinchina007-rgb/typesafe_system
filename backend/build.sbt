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

lazy val root = (project in file("."))
  .aggregate(
    sharedKernel,
    travelerDomain,
    orderDomain,
    apiGateway
  )
  .settings(
    name := "travel-platform-backend",
    publish / skip := true,
    Compile / unmanagedSourceDirectories := Seq.empty,
    Test / unmanagedSourceDirectories := Seq.empty
  )

lazy val sharedKernel = module("shared-kernel")

lazy val identityDomain = module("identity-domain")
  .dependsOn(sharedKernel)

lazy val travelerDomain = module("traveler-domain")
  .dependsOn(sharedKernel)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, munitDependency)
  )

lazy val flightDomain = module("flight-domain")
  .dependsOn(sharedKernel)

lazy val hotelDomain = module("hotel-domain")
  .dependsOn(sharedKernel)

lazy val orderDomain = module("order-domain")
  .dependsOn(sharedKernel, travelerDomain)
  .settings(
    libraryDependencies ++= Seq(catsCoreDependency, munitDependency)
  )

lazy val contentDomain = module("content-domain")
  .dependsOn(sharedKernel, orderDomain)

lazy val tourGroupDomain = module("tour-group-domain")
  .dependsOn(sharedKernel, travelerDomain)

lazy val operationsDomain = module("operations-domain")
  .dependsOn(sharedKernel, orderDomain, contentDomain, tourGroupDomain)

lazy val apiGateway = module("api-gateway")
  .dependsOn(
    sharedKernel,
    travelerDomain,
    orderDomain
  )
  .settings(
    libraryDependencies += catsEffectDependency,
    Compile / run / mainClass := Some("com.typesafe.travel.api.Main")
  )

def module(moduleName: String) =
  Project(id = moduleName, base = file(s"modules/$moduleName"))
    .settings(commonSettings)
    .settings(
      name := moduleName
    )
