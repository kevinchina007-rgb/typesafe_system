package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.persistence.operations.ManagerPlannerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant

object RegisterAirlineManagerPlanner extends ConnectionApiPlan[RegisterAirlineManagerPlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "RegisterAirlineManagerPlanner"
  override def plan(input: RegisterAirlineManagerPlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      response <- ManagerPlannerPlainSql.registerAirline(connection, input, passwordHash, Instant.now())
    yield response

object RegisterHotelManagerPlanner extends ConnectionApiPlan[RegisterHotelManagerPlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "RegisterHotelManagerPlanner"
  override def plan(input: RegisterHotelManagerPlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      response <- ManagerPlannerPlainSql.registerHotel(connection, input, passwordHash, Instant.now())
    yield response

object RegisterSiteAdminPlanner extends ConnectionApiPlan[RegisterSiteAdminPlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "RegisterSiteAdminPlanner"
  override def plan(input: RegisterSiteAdminPlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      response <- ManagerPlannerPlainSql.registerSiteAdmin(connection, input, passwordHash, Instant.now())
    yield response

object ListManagerTasksPlanner extends ConnectionApiPlan[ManagerTasksPlannerRequest, ManagerBookingTaskListPlannerResponse]:
  override val name: String = "ListManagerTasksPlanner"
  override def plan(input: ManagerTasksPlannerRequest, connection: Connection): IO[ManagerBookingTaskListPlannerResponse] =
    ManagerPlannerPlainSql.listTasks(connection, input)

object BatchConfirmManagerTasksPlanner extends ConnectionApiPlan[ManagerBatchDecisionPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "BatchConfirmManagerTasksPlanner"
  override def plan(input: ManagerBatchDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    ManagerPlannerPlainSql.batchDecision(connection, input, "confirm", Instant.now())

object BatchRejectManagerTasksPlanner extends ConnectionApiPlan[ManagerBatchDecisionPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "BatchRejectManagerTasksPlanner"
  override def plan(input: ManagerBatchDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    ManagerPlannerPlainSql.batchDecision(connection, input, "reject", Instant.now())

object ListManagerFlightsPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerFlightListPlannerResponse]:
  override val name: String = "ListManagerFlightsPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerFlightListPlannerResponse] =
    ManagerPlannerPlainSql.listFlights(connection, input)

object ListManagerHotelsPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerHotelListPlannerResponse]:
  override val name: String = "ListManagerHotelsPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerHotelListPlannerResponse] =
    ManagerPlannerPlainSql.listHotels(connection, input)

object ListManagerRefundTasksPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerRefundTaskListPlannerResponse]:
  override val name: String = "ListManagerRefundTasksPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerRefundTaskListPlannerResponse] =
    ManagerPlannerPlainSql.listRefundTasks(connection, input)

object CreateManagerFlightPlanner extends ConnectionApiPlan[CreateManagerFlightPlannerRequest, ManagerFlightPlannerResponse]:
  override val name: String = "CreateManagerFlightPlanner"
  override def plan(input: CreateManagerFlightPlannerRequest, connection: Connection): IO[ManagerFlightPlannerResponse] =
    ManagerPlannerPlainSql.createFlight(connection, input, Instant.now())

object CreateManagerRoomTypePlanner extends ConnectionApiPlan[CreateManagerRoomTypePlannerRequest, ManagerHotelPlannerResponse]:
  override val name: String = "CreateManagerRoomTypePlanner"
  override def plan(input: CreateManagerRoomTypePlannerRequest, connection: Connection): IO[ManagerHotelPlannerResponse] =
    ManagerPlannerPlainSql.createRoomType(connection, input)

object ConfirmManagerBookingItemPlanner extends ConnectionApiPlan[ManagerDecisionPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "ConfirmManagerBookingItemPlanner"
  override def plan(input: ManagerDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    ManagerPlannerPlainSql.decision(connection, input, "confirm", Instant.now())

object RejectManagerBookingItemPlanner extends ConnectionApiPlan[ManagerDecisionPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "RejectManagerBookingItemPlanner"
  override def plan(input: ManagerDecisionPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    ManagerPlannerPlainSql.decision(connection, input, "reject", Instant.now())

object ApproveManagerRefundPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "ApproveManagerRefundPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    ManagerPlannerPlainSql.refundDecision(connection, input.managerId, "approve", Instant.now())

object RejectManagerRefundPlanner extends ConnectionApiPlan[ManagerScopedPlannerRequest, ManagerBatchDecisionPlannerResponse]:
  override val name: String = "RejectManagerRefundPlanner"
  override def plan(input: ManagerScopedPlannerRequest, connection: Connection): IO[ManagerBatchDecisionPlannerResponse] =
    ManagerPlannerPlainSql.refundDecision(connection, input.managerId, "reject", Instant.now())
