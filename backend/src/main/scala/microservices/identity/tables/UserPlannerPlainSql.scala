// UserPlannerPlainSql 封装身份模块的plain SQL 实现。

package com.typesafe.travel.persistence.identity

import cats.effect.IO
import com.typesafe.travel.identity.domain.*

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.util.UUID

object UserPlannerPlainSql:
  private val selectUserSql =
    """
      select user_id, email, phone, nickname, avatar_url, membership_level, points, status, default_traveler_id, created_at
      from users
    """

  def create(connection: Connection, request: CreateUserPlannerRequest, createdAt: Instant): IO[UserPlannerResponse] =
    IO.blocking {
      val userId = s"user-${UUID.randomUUID().toString.take(12)}"
      val statement = connection.prepareStatement(
        """
          insert into users (user_id, email, phone, nickname, avatar_url, membership_level, points, status, default_traveler_id, created_at)
          values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      )
      try
        statement.setString(1, userId)
        statement.setString(2, request.email)
        statement.setString(3, request.phone)
        statement.setString(4, request.nickname)
        statement.setString(5, null)
        statement.setString(6, "Standard")
        statement.setLong(7, 0L)
        statement.setString(8, "PendingActivation")
        statement.setString(9, null)
        statement.setTimestamp(10, Timestamp.from(createdAt))
        statement.executeUpdate()
        readById(connection, userId)
      finally statement.close()
    }

  def login(connection: Connection, request: LoginPlannerRequest): IO[UserPlannerResponse] =
    IO.blocking {
      val statement = connection.prepareStatement(selectUserSql + " where email = ?")
      try
        statement.setString(1, request.email)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then readUser(resultSet)
          else throw new IllegalArgumentException(s"User '${request.email}' was not found")
        finally resultSet.close()
      finally statement.close()
    }

  def get(connection: Connection, request: GetUserPlannerRequest): IO[UserPlannerResponse] =
    IO.blocking(readById(connection, request.userId))

  def uploadAvatar(connection: Connection, request: UploadUserAvatarPlannerRequest): IO[UserPlannerResponse] =
    IO.blocking {
      val statement = connection.prepareStatement("update users set avatar_url = ? where user_id = ?")
      try
        statement.setString(1, request.publicUrl)
        statement.setString(2, request.userId)
        statement.executeUpdate()
      finally statement.close()
      readById(connection, request.userId)
    }

  def updateProfile(connection: Connection, request: UpdateUserProfilePlannerRequest): IO[UserPlannerResponse] =
    IO.blocking {
      val statement = connection.prepareStatement("update users set nickname = ?, phone = ? where user_id = ?")
      try
        statement.setString(1, request.nickname)
        statement.setString(2, request.phone)
        statement.setString(3, request.userId)
        val updatedRows = statement.executeUpdate()
        if updatedRows == 0 then throw new IllegalArgumentException(s"User '${request.userId}' was not found")
      finally statement.close()
      readById(connection, request.userId)
    }

  private def readById(connection: Connection, userId: String): UserPlannerResponse =
    val statement = connection.prepareStatement(selectUserSql + " where user_id = ?")
    try
      statement.setString(1, userId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then readUser(resultSet)
        else throw new IllegalArgumentException(s"User '$userId' was not found")
      finally resultSet.close()
    finally statement.close()

  private def readUser(resultSet: ResultSet): UserPlannerResponse =
    UserPlannerResponse(
      userId = resultSet.getString("user_id"),
      email = resultSet.getString("email"),
      nickname = resultSet.getString("nickname"),
      phone = resultSet.getString("phone"),
      avatarUrl = Option(resultSet.getString("avatar_url")),
      status = UserAccountStatus.fromText(resultSet.getString("status")).toString,
      membershipLevel = UserMembershipLevel.fromText(resultSet.getString("membership_level")).toString,
      points = resultSet.getLong("points"),
      defaultTravelerProfileId = Option(resultSet.getString("default_traveler_id")),
      createdAt = resultSet.getTimestamp("created_at").toInstant.toString
    )
