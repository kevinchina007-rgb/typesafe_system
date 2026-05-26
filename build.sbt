ThisBuild / scalaVersion := "3.4.2"
ThisBuild / version := "0.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "APP",

    // 主类
    Compile / mainClass := Some("Main"),

    // 依赖（最小 http4s 服务器）
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % "1.0.0-M44",
      "org.http4s" %% "http4s-dsl" % "1.0.0-M44",
      "ch.qos.logback" % "logback-classic" % "1.5.16"
    ),

    // assembly 设置
    assembly / mainClass := Some("Main"),

    assembly / assemblyMergeStrategy := {
      case PathList("module-info.class") => MergeStrategy.discard
      case PathList("META-INF", "services", _*) => MergeStrategy.concat
      case PathList("META-INF", _*) => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case _ => MergeStrategy.first
    }
  )