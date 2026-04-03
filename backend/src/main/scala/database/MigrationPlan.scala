package com.typesafe.travel.persistence

import scala.io.Source

final case class MigrationStep(
    version: Int,
    description: String,
    statements: List[String]
)

object MigrationPlan:
  val defaultSteps: List[MigrationStep] = List(
    loadMigrationStep(1, "baseline", "V1__baseline.sql"),
    loadMigrationStep(2, "order_and_inventory_refinement", "V2__order_and_inventory_refinement.sql"),
    loadMigrationStep(3, "manager_workflow_phase1", "V3__manager_workflow_phase1.sql"),
    loadMigrationStep(5, "flight_inventory_reservations", "V5__flight_inventory_reservations.sql"),
    loadMigrationStep(6, "hotel_inventory_locking", "V6__hotel_inventory_locking.sql"),
    loadMigrationStep(7, "train_ticket_phase1", "V7__train_ticket_phase1.sql"),
    loadMigrationStep(8, "attraction_ticket_phase1", "V8__attraction_ticket_phase1.sql"),
    loadMigrationStep(9, "tour_group_phase1", "V9__tour_group_phase1.sql"),
    loadMigrationStep(10, "blog_and_review_phase1", "V10__blog_and_review_phase1.sql"),
    loadMigrationStep(11, "blog_review_phase3", "V11__blog_review_phase3.sql"),
    loadMigrationStep(12, "attraction_ticket_inventory_fields", "V12__attraction_ticket_inventory_fields.sql"),
    loadMigrationStep(13, "authentication_session_phase1", "V13__authentication_session_phase1.sql"),
    loadMigrationStep(14, "tour_group_chat_phase1", "V14__tour_group_chat_phase1.sql"),
    loadMigrationStep(15, "tour_group_chat_phase2", "V15__tour_group_chat_phase2.sql"),
    loadMigrationStep(16, "authentication_authorization_phase2", "V16__authentication_authorization_phase2.sql"),
    loadMigrationStep(17, "search_enhancement_phase2", "V17__search_enhancement_phase2.sql"),
    loadMigrationStep(18, "train_attraction_enhancement_phase2", "V18__train_attraction_enhancement_phase2.sql")
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
