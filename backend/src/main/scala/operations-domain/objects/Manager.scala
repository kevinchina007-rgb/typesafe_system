package com.typesafe.travel.operations.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

enum ManagerStatus:
  case Active, Inactive

enum ManagerType:
  case Airline, Hotel, Attraction

sealed trait ManagerContext:
  def managerId: ManagerId
  def primaryEmailAddress: EmailAddress
  def displayName: PersonName
  def managerStatus: ManagerStatus
  def createdAt: Instant
  def managerType: ManagerType

final case class AirlineManager private[domain] (
    managerId: ManagerId,
    airlineId: AirlineId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
) extends ManagerContext:
  val managerType: ManagerType = ManagerType.Airline

object AirlineManager:
  def register(
      managerId: ManagerId,
      airlineId: AirlineId,
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): AirlineManager =
    AirlineManager(managerId, airlineId, primaryEmailAddress, displayName, ManagerStatus.Active, createdAt)

  def restore(
      managerId: ManagerId,
      airlineId: AirlineId,
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      managerStatus: ManagerStatus,
      createdAt: Instant
  ): AirlineManager =
    AirlineManager(managerId, airlineId, primaryEmailAddress, displayName, managerStatus, createdAt)

final case class HotelManager private[domain] (
    managerId: ManagerId,
    hotelId: HotelId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
) extends ManagerContext:
  val managerType: ManagerType = ManagerType.Hotel

object HotelManager:
  def register(
      managerId: ManagerId,
      hotelId: HotelId,
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): HotelManager =
    HotelManager(managerId, hotelId, primaryEmailAddress, displayName, ManagerStatus.Active, createdAt)

  def restore(
      managerId: ManagerId,
      hotelId: HotelId,
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      managerStatus: ManagerStatus,
      createdAt: Instant
  ): HotelManager =
    HotelManager(managerId, hotelId, primaryEmailAddress, displayName, managerStatus, createdAt)

final case class AttractionManager private[domain] (
    managerId: ManagerId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
) extends ManagerContext:
  val managerType: ManagerType = ManagerType.Attraction

object AttractionManager:
  def register(
      managerId: ManagerId,
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): AttractionManager =
    AttractionManager(managerId, primaryEmailAddress, displayName, ManagerStatus.Active, createdAt)

  def restore(
      managerId: ManagerId,
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      managerStatus: ManagerStatus,
      createdAt: Instant
  ): AttractionManager =
    AttractionManager(managerId, primaryEmailAddress, displayName, managerStatus, createdAt)

enum ManagerError(val message: String) extends DomainError:
  case ManagerWasNotFoundByEmail(managerType: ManagerType, primaryEmailAddress: EmailAddress)
      extends ManagerError(s"$managerType manager '${primaryEmailAddress.value}' was not found")
  case ManagerWasNotFoundById(managerType: ManagerType, managerId: ManagerId)
      extends ManagerError(s"$managerType manager '${managerId.value}' was not found")
  case ManagerEmailAlreadyExists(primaryEmailAddress: EmailAddress)
      extends ManagerError(s"Manager email '${primaryEmailAddress.value}' already exists")
  case ManagerWasInactive(managerType: ManagerType, managerId: ManagerId)
      extends ManagerError(s"$managerType manager '${managerId.value}' is inactive")
  case ManagerScopeDidNotMatch(managerType: ManagerType, managerId: ManagerId, orderItemId: OrderItemId)
      extends ManagerError(s"$managerType manager '${managerId.value}' cannot act on booking item '${orderItemId.value}'")
