package com.typesafe.travel.persistence

import cats.effect.IO

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
    loadMigrationStep(18, "train_attraction_enhancement_phase2", "V18__train_attraction_enhancement_phase2.sql"),
    loadMigrationStep(19, "uploaded_binary_assets", "V19__uploaded_binary_assets.sql"),
    loadMigrationStep(20, "feedback_chat_phase1", "V20__feedback_chat_phase1.sql"),
    loadMigrationStep(21, "advertising_phase1", "V21__advertising_phase1.sql"),
    loadMigrationStep(22, "advertising_slots", "V22__advertising_slots.sql"),
    loadMigrationStep(23, "flight_demo_schedule", "V23__flight_demo_schedule.sql"),
    loadMigrationStep(24, "flight_demo_schedule_refresh", "V24__flight_demo_schedule_refresh.sql"),
    loadMigrationStep(25, "feedback_structured_messages", "V25__feedback_structured_messages.sql"),
    loadMigrationStep(26, "clear_feedback_chat_history", "V26__clear_feedback_chat_history.sql"),
    loadMigrationStep(27, "traveler_profile_service_details", "V27__traveler_profile_service_details.sql"),
    loadMigrationStep(28, "short_blog_community", "V28__short_blog_community.sql"),
    loadMigrationStep(29, "short_blog_demo_population", "V29__short_blog_demo_population.sql"),
    loadMigrationStep(30, "blog_favorites", "V30__blog_favorites.sql"),
    loadMigrationStep(31, "blog_profile_privacy", "V31__blog_profile_privacy.sql"),
    loadMigrationStep(32, "train_booking_no_supplier_confirmation", "V32__train_booking_no_supplier_confirmation.sql"),
    loadMigrationStep(33, "train_seat_label_chinese_high_speed", "V33__train_seat_label_chinese_high_speed.sql"),
    loadMigrationStep(34, "train_reference_generated_batch_refresh", "V34__train_reference_generated_batch_refresh.sql"),
    loadMigrationStep(35, "remove_legacy_g_train_seed_rows", "V35__remove_legacy_g_train_seed_rows.sql"),
    loadMigrationStep(36, "site_admin_managers", "V36__site_admin_managers.sql"),
    loadMigrationStep(37, "advertising_creative_layout", "V37__advertising_creative_layout.sql"),
    loadMigrationStep(38, "advertising_review_feedback_settings", "V38__advertising_review_feedback_settings.sql"),
    loadMigrationStep(39, "site_admin_profile_logo", "V39__site_admin_profile_logo.sql")
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
