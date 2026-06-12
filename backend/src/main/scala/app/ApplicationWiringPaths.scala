// ApplicationWiringPaths 定义 application 装配所需的路径常量。

package com.typesafe.travel.api

import java.nio.file.{Path, Paths}

object ApplicationWiringPaths:
  def resolveConfiguredPath(primaryEnvName: String, fallbackEnvName: String, fallbackRelativeSegments: String*): Path =
    val configuredPath = sys.env.get(primaryEnvName).orElse(sys.env.get(fallbackEnvName)).map(_.trim).filter(_.nonEmpty)
    configuredPath match
      case Some(pathText) =>
        Paths.get(pathText).toAbsolutePath.normalize()
      case None =>
        Paths.get(fallbackRelativeSegments.head, fallbackRelativeSegments.tail*).toAbsolutePath.normalize()
