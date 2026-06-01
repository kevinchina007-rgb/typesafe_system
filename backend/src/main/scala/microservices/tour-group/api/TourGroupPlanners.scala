package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object CreateTourGroupPlanner extends ConnectionApiPlan[CreateTourGroupPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "CreateTourGroupPlanner"
  override def plan(input: CreateTourGroupPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.create(connection, input, java.time.Instant.now())

object ListTourGroupsPlanner extends ConnectionApiPlan[ListTourGroupsPlannerRequest, TourGroupListPlannerResponse]:
  override val name: String = "ListTourGroupsPlanner"
  override def plan(input: ListTourGroupsPlannerRequest, connection: Connection): IO[TourGroupListPlannerResponse] =
    TourGroupPlannerPlainSql.list(connection)

object GetTourGroupDetailsPlanner extends ConnectionApiPlan[TourGroupByIdPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "GetTourGroupDetailsPlanner"
  override def plan(input: TourGroupByIdPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.get(connection, input)

object JoinTourGroupPlanner extends ConnectionApiPlan[JoinTourGroupPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "JoinTourGroupPlanner"
  override def plan(input: JoinTourGroupPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.join(connection, input, java.time.Instant.now())

object LeaveTourGroupPlanner extends ConnectionApiPlan[LeaveTourGroupPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "LeaveTourGroupPlanner"
  override def plan(input: LeaveTourGroupPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.leave(connection, input, java.time.Instant.now())

object AddMembershipTravelerPlanner extends ConnectionApiPlan[AddMembershipTravelerPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "AddMembershipTravelerPlanner"
  override def plan(input: AddMembershipTravelerPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.addMembershipTraveler(connection, input, java.time.Instant.now())

object KickTourGroupMemberPlanner extends ConnectionApiPlan[KickTourGroupMemberPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "KickTourGroupMemberPlanner"
  override def plan(input: KickTourGroupMemberPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.kickMember(connection, input, java.time.Instant.now())

object BlacklistTourGroupMemberPlanner extends ConnectionApiPlan[BlacklistTourGroupMemberPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "BlacklistTourGroupMemberPlanner"
  override def plan(input: BlacklistTourGroupMemberPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.blacklistMember(connection, input, java.time.Instant.now())

object TransferTourGroupLeaderPlanner extends ConnectionApiPlan[TransferTourGroupLeaderPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "TransferTourGroupLeaderPlanner"
  override def plan(input: TransferTourGroupLeaderPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.transferOrganizer(connection, input, java.time.Instant.now())

object CreateTourGroupPlanItemPlanner extends ConnectionApiPlan[CreateTourGroupPlanItemPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "CreateTourGroupPlanItemPlanner"
  override def plan(input: CreateTourGroupPlanItemPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.createPlanItem(connection, input, java.time.Instant.now())

object CreateTourGroupPlanOptionPlanner extends ConnectionApiPlan[CreateTourGroupPlanOptionPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "CreateTourGroupPlanOptionPlanner"
  override def plan(input: CreateTourGroupPlanOptionPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.createPlanOption(connection, input, java.time.Instant.now())
