package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.shared.kernel.*

import java.time.{DayOfWeek, Instant, LocalDate}

final case class AttractionAdminSession(
    attractionManager: AttractionManager,
    managedAttractions: List[Attraction]
)

trait AttractionAdminApplicationService[F[_]]:
  def registerAttractionManager(
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): F[AttractionAdminSession]
  def loginAttractionManager(primaryEmailAddress: EmailAddress): F[AttractionAdminSession]
  def listManagedAttractions(managerId: ManagerId): F[List[Attraction]]
  def createAttraction(
      managerId: ManagerId,
      attractionName: String,
      city: String,
      location: String,
      description: String,
      createdAt: Instant
  ): F[Attraction]
  def createTicketType(
      managerId: ManagerId,
      attractionId: AttractionId,
      ticketTypeName: String,
      description: String,
      unitPrice: Money,
      availableFromDate: LocalDate,
      availableToDate: LocalDate,
      totalQuantity: Int,
      validWeekdays: Set[DayOfWeek],
      createdAt: Instant
  ): F[Attraction]
  def createTicketSession(
      managerId: ManagerId,
      attractionId: AttractionId,
      ticketTypeId: TicketTypeId,
      sessionName: String,
      useDate: LocalDate,
      startsAt: Instant,
      endsAt: Instant,
      capacity: Int,
      createdAt: Instant
  ): F[Attraction]
  def addTicketEligibilityRule(
      managerId: ManagerId,
      attractionId: AttractionId,
      ticketTypeId: TicketTypeId,
      ruleType: TicketEligibilityRuleType,
      ruleConfig: TicketEligibilityRuleConfig,
      createdAt: Instant
  ): F[Attraction]

final class LiveAttractionAdminApplicationService[F[_]: MonadThrow](
    managerService: ManagerService[F],
    attractionRepository: AttractionRepository[F]
) extends AttractionAdminApplicationService[F]:
  override def registerAttractionManager(
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): F[AttractionAdminSession] =
    managerService.registerAttractionManager(primaryEmailAddress, displayName, createdAt).flatMap(buildSession)

  override def loginAttractionManager(primaryEmailAddress: EmailAddress): F[AttractionAdminSession] =
    managerService.loginAttractionManager(primaryEmailAddress).flatMap(buildSession)

  override def listManagedAttractions(managerId: ManagerId): F[List[Attraction]] =
    managerService.loadAttractionManager(managerId) *> attractionRepository.findAttractionsByManagerId(managerId)

  override def createAttraction(
      managerId: ManagerId,
      attractionName: String,
      city: String,
      location: String,
      description: String,
      createdAt: Instant
  ): F[Attraction] =
    for
      attractionManager <- managerService.loadAttractionManager(managerId)
      attractionId <- attractionRepository.nextAttractionId
      attraction <- com.typesafe.travel.attraction.domain.createAttraction(
        attractionId,
        attractionManager.managerId,
        attractionName,
        city,
        location,
        description,
        createdAt
      ).liftTo[F]
      savedAttraction <- attractionRepository.saveAttraction(attraction)
    yield savedAttraction

  override def createTicketType(
      managerId: ManagerId,
      attractionId: AttractionId,
      ticketTypeName: String,
      description: String,
      unitPrice: Money,
      availableFromDate: LocalDate,
      availableToDate: LocalDate,
      totalQuantity: Int,
      validWeekdays: Set[DayOfWeek],
      createdAt: Instant
  ): F[Attraction] =
    for
      attraction <- loadManagedAttraction(managerId, attractionId)
      ticketTypeId <- attractionRepository.nextTicketTypeId
      ticketType <- com.typesafe.travel.attraction.domain.createTicketType(
        ticketTypeId,
        attractionId,
        ticketTypeName,
        description,
        unitPrice,
        availableFromDate,
        availableToDate,
        totalQuantity,
        validWeekdays,
        createdAt
      ).liftTo[F]
      updatedAttraction <- attraction.addTicketType(ticketType).liftTo[F]
      savedAttraction <- attractionRepository.saveAttraction(updatedAttraction)
    yield savedAttraction

  override def addTicketEligibilityRule(
      managerId: ManagerId,
      attractionId: AttractionId,
      ticketTypeId: TicketTypeId,
      ruleType: TicketEligibilityRuleType,
      ruleConfig: TicketEligibilityRuleConfig,
      createdAt: Instant
  ): F[Attraction] =
    for
      attraction <- loadManagedAttraction(managerId, attractionId)
      ticketType <- attraction.findTicketType(ticketTypeId).liftTo[F]
      ruleId <- attractionRepository.nextTicketEligibilityRuleId
      rule <- com.typesafe.travel.attraction.domain.createTicketEligibilityRule(
        ruleId,
        ticketTypeId,
        ruleType,
        ruleConfig,
        createdAt
      ).liftTo[F]
      updatedTicketType <- ticketType.addEligibilityRule(rule).liftTo[F]
      updatedAttraction <- attraction.replaceTicketType(updatedTicketType).liftTo[F]
      savedAttraction <- attractionRepository.saveAttraction(updatedAttraction)
    yield savedAttraction

  override def createTicketSession(
      managerId: ManagerId,
      attractionId: AttractionId,
      ticketTypeId: TicketTypeId,
      sessionName: String,
      useDate: LocalDate,
      startsAt: Instant,
      endsAt: Instant,
      capacity: Int,
      createdAt: Instant
  ): F[Attraction] =
    for
      attraction <- loadManagedAttraction(managerId, attractionId)
      ticketType <- attraction.findTicketType(ticketTypeId).liftTo[F]
      sessionId <- attractionRepository.nextAttractionTicketSessionId
      ticketSession <- createAttractionTicketSession(sessionId, ticketTypeId, sessionName, useDate, startsAt, endsAt, capacity, createdAt).liftTo[F]
      updatedTicketType <- ticketType.addSession(ticketSession).liftTo[F]
      updatedAttraction <- attraction.replaceTicketType(updatedTicketType).liftTo[F]
      savedAttraction <- attractionRepository.saveAttraction(updatedAttraction)
    yield savedAttraction

  private def loadManagedAttraction(managerId: ManagerId, attractionId: AttractionId): F[Attraction] =
    for
      attractionManager <- managerService.loadAttractionManager(managerId)
      attraction <- attractionRepository.findAttractionById(attractionId).flatMap(_.liftTo[F](AttractionError.AttractionWasNotFound(attractionId)))
      _ <- if attraction.managerId == attractionManager.managerId then ().pure[F]
      else MonadThrow[F].raiseError(AttractionError.AttractionWasNotOwnedByManager(attractionId, managerId))
    yield attraction

  private def buildSession(attractionManager: AttractionManager): F[AttractionAdminSession] =
    attractionRepository.findAttractionsByManagerId(attractionManager.managerId).map(AttractionAdminSession(attractionManager, _))

