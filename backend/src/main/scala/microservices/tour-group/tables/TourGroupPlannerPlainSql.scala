package com.typesafe.travel.tourgroup.domain

import cats.effect.IO

import java.sql.Connection
import java.time.Instant

object TourGroupPlannerPlainSql:
  def create(connection: Connection, input: CreateTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlPlanning.create(connection, input, now)

  def list(connection: Connection): IO[TourGroupListPlannerResponse] =
    TourGroupPlannerPlainSqlPlanning.list(connection)

  def get(connection: Connection, input: TourGroupByIdPlannerRequest): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlPlanning.get(connection, input)

  def join(connection: Connection, input: JoinTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlPlanning.join(connection, input, now)

  def leave(connection: Connection, input: LeaveTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlPlanning.leave(connection, input, now)

  def addMembershipTraveler(connection: Connection, input: AddMembershipTravelerPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlPlanning.addMembershipTraveler(connection, input, now)

  def removeMembershipTraveler(connection: Connection, input: RemoveMembershipTravelerPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlPlanning.removeMembershipTraveler(connection, input, now)

  def kickMember(connection: Connection, input: KickTourGroupMemberPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlPlanning.kickMember(connection, input, now)

  def blacklistMember(connection: Connection, input: BlacklistTourGroupMemberPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlPlanning.blacklistMember(connection, input, now)

  def transferOrganizer(connection: Connection, input: TransferTourGroupLeaderPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlPlanning.transferOrganizer(connection, input, now)

  def createPlanItem(connection: Connection, input: CreateTourGroupPlanItemPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlSelection.createPlanItem(connection, input, now)

  def createPlanOption(connection: Connection, input: CreateTourGroupPlanOptionPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlSelection.createPlanOption(connection, input, now)

  def createSelection(connection: Connection, input: CreateTourGroupSelectionPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlSelection.createSelection(connection, input, now)

  def submitSelection(connection: Connection, input: SubmitTourGroupSelectionPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlSelection.submitSelection(connection, input, now)
