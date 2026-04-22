package com.typesafe.travel.operations.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

final case class ManagerStatus(value: String):
  override def toString: String = value

object ManagerStatus:
  val Active: ManagerStatus = ManagerStatus("Active")
  val Inactive: ManagerStatus = ManagerStatus("Inactive")

  def fromText(value: String): ManagerStatus =
    value.trim.toLowerCase match
      case "inactive" => Inactive
      case _ => Active

final case class ManagerType(value: String):
  override def toString: String = value

object ManagerType:
  val Airline: ManagerType = ManagerType("Airline")
  val Hotel: ManagerType = ManagerType("Hotel")
  val Attraction: ManagerType = ManagerType("Attraction")

  def fromText(value: String): ManagerType =
    value.trim.toLowerCase match
      case "hotel" => Hotel
      case "attraction" => Attraction
      case _ => Airline

sealed trait ManagerContext:
  def managerId: ManagerId
  def primaryEmailAddress: EmailAddress
  def displayName: PersonName
  def managerStatus: ManagerStatus
  def createdAt: Instant
  def managerType: ManagerType

final case class AirlineManager(
    managerId: ManagerId,
    airlineId: AirlineId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
) extends ManagerContext:
  val managerType: ManagerType = ManagerType.Airline

final case class HotelManager(
    managerId: ManagerId,
    hotelId: HotelId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
) extends ManagerContext:
  val managerType: ManagerType = ManagerType.Hotel

final case class AttractionManager(
    managerId: ManagerId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
) extends ManagerContext:
  val managerType: ManagerType = ManagerType.Attraction

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

sealed trait ManagerError extends DomainError:
  def message: String

object ManagerError:
  final case class ManagerWasNotFoundByEmail(managerType: ManagerType, primaryEmailAddress: EmailAddress) extends ManagerError:
    override val message: String = s"$managerType manager '${primaryEmailAddress.value}' was not found"

  final case class ManagerWasNotFoundById(managerType: ManagerType, managerId: ManagerId) extends ManagerError:
    override val message: String = s"$managerType manager '${managerId.value}' was not found"

  final case class ManagerEmailAlreadyExists(primaryEmailAddress: EmailAddress) extends ManagerError:
    override val message: String = s"Manager email '${primaryEmailAddress.value}' already exists"

  final case class ManagerWasInactive(managerType: ManagerType, managerId: ManagerId) extends ManagerError:
    override val message: String = s"$managerType manager '${managerId.value}' is inactive"

  final case class ManagerScopeDidNotMatch(managerType: ManagerType, managerId: ManagerId, orderItemId: OrderItemId) extends ManagerError:
    override val message: String = s"$managerType manager '${managerId.value}' cannot act on booking item '${orderItemId.value}'"