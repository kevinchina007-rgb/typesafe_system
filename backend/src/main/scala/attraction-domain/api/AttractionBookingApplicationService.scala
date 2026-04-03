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
  def suggestAttractions(keyword: String): F[List[SearchSuggestion]]
  def getAttractionDetails(attractionId: AttractionId): F[Attraction]
  def addAttractionItemToOrder(
      actingUserId: UserId,
      orderId: OrderId,
      attractionId: AttractionId,
      ticketTypeId: TicketTypeId,
      sessionId: Option[AttractionTicketSessionId],
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
        case Some(query) =>
          attractions
            .filter(attraction => TravelSearchAliases.matchesAttractionQuery(attraction.attractionName, attraction.city, attraction.location, query))
            .sortBy(attraction => -attractionSearchScore(attraction, query))
        case None        => attractions
    }

  override def suggestAttractions(keyword: String): F[List[SearchSuggestion]] =
    SearchRanking.usableKeyword(keyword) match
      case None => MonadThrow[F].pure(List.empty)
      case Some(normalizedKeyword) =>
        attractionRepository
          .listPublishedAttractions
          .map(
            _.flatMap { attraction =>
              val attractionScore = SearchRanking.weightedScore(normalizedKeyword, attraction.attractionName -> 4, attraction.city -> 2, attraction.location -> 2)
              val cityScore = SearchRanking.weightedScore(normalizedKeyword, attraction.city -> 4, attraction.attractionName -> 2)
              List(
                Option.when(attractionScore > 0)(
                  SearchSuggestion(
                    resourceType = SearchResourceType.Attraction,
                    value = attraction.attractionName,
                    title = attraction.attractionName,
                    subtitle = s"${attraction.city} · ${attraction.location}",
                    score = attractionScore
                  )
                ),
                Option.when(cityScore > 0)(
                  SearchSuggestion(
                    resourceType = SearchResourceType.Attraction,
                    value = attraction.city,
                    title = attraction.city,
                    subtitle = attraction.attractionName,
                    score = cityScore
                  )
                )
              ).flatten
            }
          )
          .map(suggestions => SearchRanking.topDistinctByValue(suggestions, 8))

  override def getAttractionDetails(attractionId: AttractionId): F[Attraction] =
    attractionRepository.findAttractionById(attractionId).flatMap(_.liftTo[F](AttractionError.AttractionWasNotFound(attractionId)))

  override def addAttractionItemToOrder(
      actingUserId: UserId,
      orderId: OrderId,
      attractionId: AttractionId,
      ticketTypeId: TicketTypeId,
      sessionId: Option[AttractionTicketSessionId],
      travelerIds: List[TravelerId],
      useDate: LocalDate,
      now: Instant
  ): F[Order] =
    for
      validatedTravelerIds <- validateTravelerSelection(travelerIds)
      travelerProfiles <- validatedTravelerIds.traverse(loadOwnedTravelerProfile(actingUserId, _))
      attraction <- getAttractionDetails(attractionId)
      ticketType <- attraction.findTicketType(ticketTypeId).liftTo[F]
      selectedSession <- sessionId match
        case Some(value) =>
          ticketType.sessions.find(_.sessionId == value).liftTo[F](AttractionError.AttractionTicketSessionWasNotFound(value))
            .flatTap(session => if session.isActive then ().pure[F] else MonadThrow[F].raiseError(AttractionError.AttractionTicketSessionWasInactive(session.sessionId)))
            .map(Some(_))
        case None => MonadThrow[F].pure(None)
      _ <- if ticketType.isActive then ().pure[F] else MonadThrow[F].raiseError(AttractionError.TicketTypeWasInactive(ticketTypeId))
      _ <- if ticketType.supportsUseDate(useDate) then ().pure[F]
      else MonadThrow[F].raiseError(AttractionError.TicketTypeUseDateWasUnavailable(ticketTypeId, useDate))
      _ <- ensureTicketTypeInventoryAvailable(ticketType, useDate, travelerProfiles.size, selectedSession)
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
          sessionId = selectedSession.map(_.sessionId),
          sessionName = selectedSession.map(_.sessionName),
          sessionStartsAt = selectedSession.map(_.startsAt),
          sessionEndsAt = selectedSession.map(_.endsAt),
          useDate = useDate,
          travelerIds = travelerProfiles.map(_.travelerId).toVector,
          unitPriceSnapshot = ticketType.unitPrice,
          ruleSummaries = ticketType.eligibilityRules.map(_.humanReadableSummary),
          eligibilityValidatedAt = now
        )
      )
    yield savedOrder

  private def ensureTicketTypeInventoryAvailable(
      ticketType: TicketType,
      useDate: LocalDate,
      requestedQuantity: Int,
      session: Option[AttractionTicketSession]
  ): F[Unit] =
    orderRepository.findAllOrders.flatMap { orders =>
      val soldQuantity = orders.iterator
        .filter(order => Set(OrderStatus.Confirmed, OrderStatus.PartiallyRefunded, OrderStatus.Refunded).contains(order.orderStatus))
        .flatMap(_.orderLineItems.iterator)
        .collect {
          case attractionOrderItem: AttractionOrderItem
              if attractionOrderItem.orderItemStatus != OrderItemStatus.Cancelled &&
                attractionOrderItem.attractionTicketSnapshot.ticketTypeId == ticketType.ticketTypeId &&
                attractionOrderItem.attractionTicketSnapshot.useDate == useDate &&
                attractionOrderItem.attractionTicketSnapshot.sessionId == session.map(_.sessionId) =>
            attractionOrderItem.attractionTicketSnapshot.travelerIds.size
        }
        .sum
      session match
        case Some(ticketSession) =>
          val remainingQuantity = (ticketSession.capacity - soldQuantity).max(0)
          if remainingQuantity >= requestedQuantity then ().pure[F]
          else MonadThrow[F].raiseError(AttractionError.AttractionTicketSessionInventoryWasNotAvailable(ticketSession.sessionId, requestedQuantity, remainingQuantity))
        case None =>
          val remainingQuantity = (ticketType.totalQuantity - soldQuantity).max(0)
          if remainingQuantity >= requestedQuantity then ().pure[F]
          else MonadThrow[F].raiseError(AttractionError.TicketTypeInventoryWasNotAvailable(ticketType.ticketTypeId, useDate, requestedQuantity, remainingQuantity))
    }

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

  private def attractionSearchScore(attraction: Attraction, keyword: String): Int =
    SearchRanking.weightedScore(keyword, attraction.attractionName -> 4, attraction.city -> 3, attraction.location -> 2)
