package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.TravelerProfileRepository

import java.time.{Instant, LocalDate}

enum AttractionBookingApplicationError(val message: String) extends DomainError:
  case OrderWasNotOwnedByUser(orderId: OrderId, actingUserId: UserId)
      extends AttractionBookingApplicationError(s"Order '${orderId.value}' does not belong to user '${actingUserId.value}'")

trait AttractionBookingApplicationService[F[_]]:
  def browseAttractions(cityQuery: Option[String]): F[List[Attraction]]
  def getAttractionDetails(attractionId: AttractionId): F[Attraction]
  def addAttractionItemToOrder(
      actingUserId: UserId,
      orderId: OrderId,
      attractionId: AttractionId,
      ticketTypeId: TicketTypeId,
      travelerIds: List[TravelerId],
      useDate: LocalDate,
      now: Instant
  ): F[Order]

final class LiveAttractionBookingApplicationService[F[_]: MonadThrow](
    attractionRepository: AttractionRepository[F],
    ticketEligibilityService: TicketEligibilityService[F],
    orderRepository: OrderRepository[F],
    orderService: OrderService[F],
    travelerProfileRepository: TravelerProfileRepository[F]
) extends AttractionBookingApplicationService[F]:
  override def browseAttractions(cityQuery: Option[String]): F[List[Attraction]] =
    attractionRepository.listPublishedAttractions.map { attractions =>
      cityQuery.map(_.trim).filter(_.nonEmpty) match
        case Some(query) => attractions.filter(_.city.toLowerCase.contains(query.toLowerCase))
        case None        => attractions
    }

  override def getAttractionDetails(attractionId: AttractionId): F[Attraction] =
    attractionRepository.findAttractionById(attractionId).flatMap(_.liftTo[F](AttractionError.AttractionWasNotFound(attractionId)))

  override def addAttractionItemToOrder(
      actingUserId: UserId,
      orderId: OrderId,
      attractionId: AttractionId,
      ticketTypeId: TicketTypeId,
      travelerIds: List[TravelerId],
      useDate: LocalDate,
      now: Instant
  ): F[Order] =
    for
      validatedTravelerIds <- validateTravelerSelection(travelerIds)
      travelerProfiles <- validatedTravelerIds.traverse(loadOwnedTravelerProfile(actingUserId, _))
      attraction <- getAttractionDetails(attractionId)
      ticketType <- attraction.findTicketType(ticketTypeId).liftTo[F]
      _ <- if ticketType.isActive then ().pure[F] else MonadThrow[F].raiseError(AttractionError.TicketTypeWasInactive(ticketTypeId))
      eligibilityResults <- travelerProfiles.traverse(ticketEligibilityService.evaluateTraveler(_, ticketType, useDate))
      _ <- ensureAllEligible(ticketTypeId, eligibilityResults)
      order <- orderRepository.findOrderById(orderId).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(orderId)))
      _ <- if order.ownerUserId == actingUserId then ().pure[F]
      else MonadThrow[F].raiseError(AttractionBookingApplicationError.OrderWasNotOwnedByUser(orderId, actingUserId))
      savedOrder <- orderService.addAttractionOrderItem(
        orderId = order.orderId,
        attractionTicketSnapshot = AttractionTicketSnapshot(
          attractionId = attraction.attractionId,
          managerId = attraction.managerId,
          attractionName = attraction.attractionName,
          ticketTypeId = ticketType.ticketTypeId,
          ticketTypeName = ticketType.ticketTypeName,
          useDate = useDate,
          travelerIds = travelerProfiles.map(_.travelerId).toVector,
          unitPriceSnapshot = ticketType.unitPrice,
          ruleSummaries = ticketType.eligibilityRules.map(_.humanReadableSummary),
          eligibilityValidatedAt = now
        )
      )
    yield savedOrder

  private def validateTravelerSelection(travelerIds: List[TravelerId]): F[List[TravelerId]] =
    if travelerIds.isEmpty then MonadThrow[F].raiseError(AttractionError.AttractionTravelerSelectionWasInvalid("At least one traveler must be selected"))
    else if travelerIds.distinct.size != travelerIds.size then
      MonadThrow[F].raiseError(AttractionError.AttractionTravelerSelectionWasInvalid("Traveler selection contains duplicates"))
    else MonadThrow[F].pure(travelerIds)

  private def loadOwnedTravelerProfile(
      actingUserId: UserId,
      travelerId: TravelerId
  ): F[com.typesafe.travel.traveler.domain.TravelerProfile] =
    travelerProfileRepository.findTravelerProfileById(travelerId).flatMap {
      case Some(travelerProfile) if travelerProfile.ownerUserId == actingUserId =>
        MonadThrow[F].pure(travelerProfile)
      case _ =>
        MonadThrow[F].raiseError(
          AttractionError.AttractionTravelerSelectionWasInvalid(
            s"Traveler '${travelerId.value}' is not available for user '${actingUserId.value}'"
          )
        )
    }

  private def ensureAllEligible(
      ticketTypeId: TicketTypeId,
      eligibilityResults: List[TicketEligibilityResult]
  ): F[Unit] =
    eligibilityResults.find(!_.eligible) match
      case Some(ineligibleTraveler) =>
        MonadThrow[F].raiseError(
          AttractionError.AttractionTravelerWasNotEligible(
            ticketTypeId,
            ineligibleTraveler.travelerId,
            ineligibleTraveler.failureReasons.mkString("; ")
          )
        )
      case None =>
        MonadThrow[F].pure(())
