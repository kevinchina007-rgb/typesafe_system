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

