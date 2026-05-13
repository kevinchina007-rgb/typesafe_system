package com.typesafe.travel.identity.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.identity.UserPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CreateUserPlanner extends ConnectionApiPlan[CreateUserPlannerRequest, UserPlannerResponse]:
  override val name: String = "CreateUserPlanner"

  override def plan(input: CreateUserPlannerRequest, connection: Connection): IO[UserPlannerResponse] =
    UserPlannerPlainSql.create(connection, input, Instant.now())

object LoginUserPlanner extends ConnectionApiPlan[LoginUserPlannerRequest, UserPlannerResponse]:
  override val name: String = "LoginUserPlanner"

  override def plan(input: LoginUserPlannerRequest, connection: Connection): IO[UserPlannerResponse] =
    UserPlannerPlainSql.login(connection, input)

object GetUserPlanner extends ConnectionApiPlan[GetUserPlannerRequest, UserPlannerResponse]:
  override val name: String = "GetUserPlanner"

  override def plan(input: GetUserPlannerRequest, connection: Connection): IO[UserPlannerResponse] =
    UserPlannerPlainSql.get(connection, input)

object UploadUserAvatarPlanner extends ConnectionApiPlan[UploadUserAvatarPlannerRequest, UserPlannerResponse]:
  override val name: String = "UploadUserAvatarPlanner"

  override def plan(input: UploadUserAvatarPlannerRequest, connection: Connection): IO[UserPlannerResponse] =
    UserPlannerPlainSql.uploadAvatar(connection, input)
