package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum RailwayManagerStatus:
  case Active, Inactive

final case class RailwayManager private[domain] (
    managerId: ManagerId,
    operatorCode: String,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: RailwayManagerStatus,
    createdAt: Instant
)

object RailwayManager:
  def register(
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

  def restore(
      managerId: ManagerId,
      operatorCode: String,
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      managerStatus: RailwayManagerStatus,
      createdAt: Instant
  ): RailwayManager =
    RailwayManager(managerId, operatorCode, primaryEmailAddress, displayName, managerStatus, createdAt)

