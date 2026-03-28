package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum RailwayManagerStatus:
  case Active, Inactive

final case class RailwayManager private (
    managerId: ManagerId,
    operatorCode: String,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: RailwayManagerStatus,
    createdAt: Instant
)

object RailwayManager:
  def registerNewRailwayManager(
      managerId: ManagerId,
      operatorCode: String,
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): RailwayManager =
    RailwayManager(
      managerId = managerId,
      operatorCode = operatorCode.trim.toUpperCase,
      primaryEmailAddress = primaryEmailAddress,
      displayName = displayName,
      managerStatus = RailwayManagerStatus.Active,
      createdAt = createdAt
    )

  def restorePersistedRailwayManager(
      managerId: ManagerId,
      operatorCode: String,
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      managerStatus: RailwayManagerStatus,
      createdAt: Instant
  ): RailwayManager =
    RailwayManager(managerId, operatorCode, primaryEmailAddress, displayName, managerStatus, createdAt)
