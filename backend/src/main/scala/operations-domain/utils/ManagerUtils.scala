package com.typesafe.travel.operations.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

def registerNewAirlineManager(
    managerId: ManagerId,
    airlineId: AirlineId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    createdAt: Instant
): AirlineManager =
  AirlineManager.register(managerId, airlineId, primaryEmailAddress, displayName, createdAt)


def restorePersistedAirlineManager(
    managerId: ManagerId,
    airlineId: AirlineId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
): AirlineManager =
  AirlineManager.restore(managerId, airlineId, primaryEmailAddress, displayName, managerStatus, createdAt)


def registerNewHotelManager(
    managerId: ManagerId,
    hotelId: HotelId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    createdAt: Instant
): HotelManager =
  HotelManager.register(managerId, hotelId, primaryEmailAddress, displayName, createdAt)


def restorePersistedHotelManager(
    managerId: ManagerId,
    hotelId: HotelId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
): HotelManager =
  HotelManager.restore(managerId, hotelId, primaryEmailAddress, displayName, managerStatus, createdAt)


def registerNewAttractionManager(
    managerId: ManagerId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    createdAt: Instant
): AttractionManager =
  AttractionManager.register(managerId, primaryEmailAddress, displayName, createdAt)


def restorePersistedAttractionManager(
    managerId: ManagerId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
): AttractionManager =
  AttractionManager.restore(managerId, primaryEmailAddress, displayName, managerStatus, createdAt)
