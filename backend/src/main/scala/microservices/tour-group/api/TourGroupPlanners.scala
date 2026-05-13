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

object AddMembershipTravelerPlanner extends ConnectionApiPlan[AddMembershipTravelerPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "AddMembershipTravelerPlanner"
  override def plan(input: AddMembershipTravelerPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.addMembershipTraveler(connection, input, java.time.Instant.now())
