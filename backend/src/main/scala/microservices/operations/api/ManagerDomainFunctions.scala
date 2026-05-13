package com.typesafe.travel.operations.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

def registerAirlineManager(
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

def registerHotelManager(
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

def registerAttractionManager(
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

def managerContextId(managerContext: ManagerContext): ManagerId =
  managerContext match
    case airlineManager: AirlineManager       => airlineManager.managerId
    case hotelManager: HotelManager           => hotelManager.managerId
    case attractionManager: AttractionManager => attractionManager.managerId

def managerContextStatus(managerContext: ManagerContext): ManagerStatus =
  managerContext match
    case airlineManager: AirlineManager       => airlineManager.managerStatus
    case hotelManager: HotelManager           => hotelManager.managerStatus
    case attractionManager: AttractionManager => attractionManager.managerStatus

def managerContextType(managerContext: ManagerContext): ManagerType =
  managerContext match
    case _: AirlineManager    => ManagerType.Airline
    case _: HotelManager      => ManagerType.Hotel
    case _: AttractionManager => ManagerType.Attraction
