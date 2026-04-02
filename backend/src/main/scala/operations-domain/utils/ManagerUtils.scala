package com.typesafe.travel.operations.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import java.time.Instant

def registerNewAirlineManager(
    managerId: ManagerId,
    airlineId: AirlineId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    createdAt: Instant
): AirlineManager =
  AirlineManager(managerId, airlineId, primaryEmailAddress, displayName, ManagerStatus.Active, createdAt)


def restorePersistedAirlineManager(
    managerId: ManagerId,
    airlineId: AirlineId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
): AirlineManager =
  AirlineManager(managerId, airlineId, primaryEmailAddress, displayName, managerStatus, createdAt)


def registerNewHotelManager(
    managerId: ManagerId,
    hotelId: HotelId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    createdAt: Instant
): HotelManager =
  HotelManager(managerId, hotelId, primaryEmailAddress, displayName, ManagerStatus.Active, createdAt)


def restorePersistedHotelManager(
    managerId: ManagerId,
    hotelId: HotelId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
): HotelManager =
  HotelManager(managerId, hotelId, primaryEmailAddress, displayName, managerStatus, createdAt)


def registerNewAttractionManager(
    managerId: ManagerId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    createdAt: Instant
): AttractionManager =
  AttractionManager(managerId, primaryEmailAddress, displayName, ManagerStatus.Active, createdAt)


def restorePersistedAttractionManager(
    managerId: ManagerId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
): AttractionManager =
  AttractionManager(managerId, primaryEmailAddress, displayName, managerStatus, createdAt)
