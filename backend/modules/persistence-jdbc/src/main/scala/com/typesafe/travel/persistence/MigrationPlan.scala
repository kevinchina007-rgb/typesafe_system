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
    loadMigrationStep(2, "order_and_inventory_refinement", "db/migrations/V2__order_and_inventory_refinement.sql"),
    loadMigrationStep(3, "manager_workflow_phase1", "db/migrations/V3__manager_workflow_phase1.sql"),
    loadMigrationStep(5, "flight_inventory_reservations", "db/migrations/V5__flight_inventory_reservations.sql"),
    loadMigrationStep(6, "hotel_inventory_locking", "db/migrations/V6__hotel_inventory_locking.sql"),
    loadMigrationStep(7, "train_ticket_phase1", "db/migrations/V7__train_ticket_phase1.sql"),
    loadMigrationStep(8, "attraction_ticket_phase1", "db/migrations/V8__attraction_ticket_phase1.sql"),
    loadMigrationStep(9, "tour_group_phase1", "db/migrations/V9__tour_group_phase1.sql")
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
