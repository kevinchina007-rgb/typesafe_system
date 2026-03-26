package com.typesafe.travel.persistence

import scala.io.Source

final case class MigrationStep(
    version: Int,
    description: String,
    statements: List[String]
)

object MigrationPlan:
  val defaultSteps: List[MigrationStep] = List(
    loadMigrationStep(1, "baseline", "db/migrations/V1__baseline.sql"),
    loadMigrationStep(2, "order_and_inventory_refinement", "db/migrations/V2__order_and_inventory_refinement.sql")
  )

  private def loadMigrationStep(version: Int, description: String, resourcePath: String): MigrationStep =
    val migrationSource = Source.fromResource(resourcePath)
    try
      MigrationStep(
        version = version,
        description = description,
        statements = migrationSource.getLines().mkString("\n").split(";").toList.map(_.trim).filter(_.nonEmpty)
      )
    finally migrationSource.close()
