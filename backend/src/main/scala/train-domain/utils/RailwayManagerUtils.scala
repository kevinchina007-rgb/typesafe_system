package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

def registerNewRailwayManager(
    managerId: ManagerId,
    operatorCode: String,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    createdAt: Instant
): RailwayManager =
  RailwayManager.register(managerId, operatorCode, primaryEmailAddress, displayName, createdAt)


def restorePersistedRailwayManager(
    managerId: ManagerId,
    operatorCode: String,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: RailwayManagerStatus,
    createdAt: Instant
): RailwayManager =
  RailwayManager.restore(managerId, operatorCode, primaryEmailAddress, displayName, managerStatus, createdAt)
