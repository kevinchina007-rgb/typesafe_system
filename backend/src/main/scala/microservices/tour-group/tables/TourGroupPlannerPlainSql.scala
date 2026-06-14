// 这个文件是 tour-group 后端的 planner 级 Plain SQL 入口聚合层。
// 它把创建团体、加入/退出、成员增删、黑名单、转让、计划项和选择等动作统一对外暴露，真正的 SQL 细节拆在更薄的 support/子文件里。
// 前端不需要也不应该镜像这个文件；前端只应对齐同名 planner API 文件和它返回的 request/response 对象。
package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.tourgroup.domain.*

import java.sql.Connection
import java.time.Instant

object TourGroupPlannerPlainSql:
  def create(connection: Connection, input: CreateTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSqlPlanning.create(connection, input, now)

  def list(connection: Connection): IO[TourGroupListResponse] =
    TourGroupPlannerPlainSqlPlanning.list(connection)

  def get(connection: Connection, input: GetTourGroupDetailsPlannerRequest): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSqlPlanning.get(connection, input)

  def join(connection: Connection, input: JoinTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSqlPlanning.join(connection, input, now)

  def leave(connection: Connection, input: LeaveTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSqlPlanning.leave(connection, input, now)

  def addMembershipTraveler(connection: Connection, input: AddMembershipTravelerPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSqlPlanning.addMembershipTraveler(connection, input, now)

  def removeMembershipTraveler(connection: Connection, input: RemoveMembershipTravelerPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSqlPlanning.removeMembershipTraveler(connection, input, now)

  def kickMember(connection: Connection, input: KickTourGroupMemberPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSqlPlanning.kickMember(connection, input, now)

  def blacklistMember(connection: Connection, input: BlacklistTourGroupMemberPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSqlPlanning.blacklistMember(connection, input, now)

  def transferOrganizer(connection: Connection, input: TransferTourGroupLeaderPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSqlPlanning.transferOrganizer(connection, input, now)

  def createPlanItem(connection: Connection, input: CreateTourGroupPlanItemPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSqlSelection.createPlanItem(connection, input, now)

  def createPlanOption(connection: Connection, input: CreateTourGroupPlanOptionPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSqlSelection.createPlanOption(connection, input, now)

  def createSelection(connection: Connection, input: CreateTourGroupSelectionPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSqlSelection.createSelection(connection, input, now)

  def submitSelection(connection: Connection, input: SubmitTourGroupSelectionPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSqlSelection.submitSelection(connection, input, now)
