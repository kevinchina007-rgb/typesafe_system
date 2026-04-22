package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.effect.LiftIO
import cats.syntax.all.*
import com.typesafe.travel.identity.domain.UserRepository
import com.typesafe.travel.order.domain.{Order, OrderError, OrderService, PaymentMethod, PaymentStatus, RefundStatus, SupplierReviewStatus}
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.tourgroup.domain.*
import com.typesafe.travel.train.domain.{TrainSeatClass, TrainStationCode}
import com.typesafe.travel.traveler.domain.TravelerProfileRepository
import java.time.{Instant, LocalDate, ZoneOffset}


trait LiveTourGroupSelectionSupport[F[_]: MonadThrow: LiftIO]:
  self: LiveTourGroupApplicationService[F] =>
  protected def mutateSelection(
      selectionId: GroupPlanSelectionId
  )(
      updater: (TourGroupDetails, GroupPlanSelection) => Either[TourGroupError, GroupPlanSelection]
  ): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetailsBySelection(selectionId)
      selection <- details.selectionById(selectionId).liftTo[F]
      updatedSelection <- updater(details, selection).liftTo[F]
      _ <- tourGroupRepository.saveSelection(updatedSelection)
      refreshed <- getGroupDetails(details.group.groupId)
    yield refreshed

  protected def batchUpdateSelections(
      selectionIds: List[GroupPlanSelectionId]
  )(
      updater: (TourGroupDetails, GroupPlanSelection) => Either[TourGroupError, GroupPlanSelection]
  ): F[TourGroupDetails] =
    for
      normalizedSelectionIds <- normalizeSelectionIds(selectionIds).liftTo[F]
      anchorDetails <- loadGroupDetailsBySelection(normalizedSelectionIds.head)
      _ <- normalizedSelectionIds.traverse_ { selectionId =>
        for
          details <- loadGroupDetailsBySelection(selectionId)
          _ <- if details.group.groupId == anchorDetails.group.groupId then ().pure[F]
          else TourGroupError.SelectionWasNotFound(selectionId).raiseError[F, Unit]
          selection <- details.selectionById(selectionId).liftTo[F]
          updatedSelection <- updater(anchorDetails, selection).liftTo[F]
          _ <- tourGroupRepository.saveSelection(updatedSelection)
        yield ()
      }
      refreshedDetails <- loadGroupDetails(anchorDetails.group.groupId)
    yield refreshedDetails

  protected def loadGroupDetails(groupId: TourGroupId): F[TourGroupDetails] =
    tourGroupRepository.findGroupDetails(groupId).flatMap(_.liftTo[F](TourGroupError.GroupWasNotFound(groupId)))

  protected def loadGroupDetailsBySelection(selectionId: GroupPlanSelectionId): F[TourGroupDetails] =
    tourGroupRepository
      .findSelectionById(selectionId)
      .flatMap(_.liftTo[F](TourGroupError.SelectionWasNotFound(selectionId)))
      .flatMap(selection => loadGroupDetails(selection.groupId))

  protected def loadLinkedOrders(links: Vector[GroupSelectionOrderLink]): F[Map[GroupPlanSelectionId, Order]] =
    links.toList.traverse { link =>
      orderRepository.findOrderById(link.orderId).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(link.orderId))).map(order => link.selectionId -> order)
    }.map(_.toMap)

  protected def ensureTravelerBelongsToUser(userId: UserId, travelerId: TravelerId): F[Unit] =
    travelerProfileRepository.findTravelerProfileById(travelerId).flatMap {
      case None => TourGroupApplicationError.MembershipTravelerWasNotFound(travelerId).raiseError[F, Unit]
      case Some(travelerProfile) if travelerProfile.ownerUserId == userId => ().pure[F]
      case Some(_) => TourGroupApplicationError.MembershipTravelerWasNotOwnedByUser(travelerId, userId).raiseError[F, Unit]
    }

  protected def ensureSelectionTravelers(
      details: TourGroupDetails,
      membershipId: TourGroupMembershipId,
      travelerIds: List[TravelerId]
  ): F[Unit] =
    if travelerIds.distinct.isEmpty then TourGroupError.SelectionTravelerWasEmpty(GroupPlanItemId("plan-item")).raiseError[F, Unit]
    else
      travelerIds.distinct.traverse_ { travelerId =>
        if details.membershipTravelers.exists(mt => mt.membershipId == membershipId && mt.travelerId == travelerId && mt.status == TourGroupMembershipTravelerStatus.Active) then
          ().pure[F]
        else
          TourGroupError.SelectionTravelerWasNotInMembership(GroupPlanSelectionId("selection"), travelerId).raiseError[F, Unit]
      }

  protected def ensureSelectionQuantity(
      itemType: GroupPlanItemType,
      quantity: Int,
      travelerCount: Int,
      selectionId: GroupPlanSelectionId
  ): Either[TourGroupError, Unit] =
    if quantity <= 0 then Left(TourGroupError.SelectionQuantityWasInvalid(quantity))
    else
      itemType match
        case GroupPlanItemType.Flight | GroupPlanItemType.Train | GroupPlanItemType.Attraction =>
          Either.cond(quantity == travelerCount, (), TourGroupError.SelectionQuantityDidNotMatchTravelerCount(selectionId, quantity, travelerCount))
        case GroupPlanItemType.Hotel =>
          Right(())

  protected def ensureOptionResourceTypeMatchesPlanItem(
      planItem: GroupPlanItem,
      resourceType: GroupPlanOptionResourceType
  ): Either[TourGroupError, Unit] =
    val compatible =
      (planItem.itemType, resourceType) match
        case (GroupPlanItemType.Flight, GroupPlanOptionResourceType.Flight)                 => true
        case (GroupPlanItemType.Hotel, GroupPlanOptionResourceType.HotelRoomType)           => true
        case (GroupPlanItemType.Train, GroupPlanOptionResourceType.TrainJourneySeat)        => true
        case (GroupPlanItemType.Attraction, GroupPlanOptionResourceType.AttractionTicketType) => true
        case _                                                                              => false
    Either.cond(compatible, (), TourGroupError.PlanOptionResourceTypeDidNotMatchPlanItem(planItem.planItemId, planItem.itemType, resourceType))

  protected def ensureSelectionOrderLinked(
      details: TourGroupDetails,
      selection: GroupPlanSelection,
      currentTime: Instant
  ): F[Unit] =
    tourGroupRepository.findSelectionOrderLinkBySelectionId(selection.selectionId).flatMap {
      case Some(_) =>
        ().pure[F]
      case None =>
        for
          membership <- details.membershipById(selection.membershipId).liftTo[F]
          planItem <- details.planItemById(selection.planItemId).liftTo[F]
          option <- details.planOptionById(selection.optionId).liftTo[F]
          travelerIds = details.selectionTravelersFor(selection.selectionId).map(_.travelerId).toList
          _ <- ensureSelectionTravelers(details, membership.membershipId, travelerIds)
          _ <- ensureSelectionQuantity(planItem.itemType, selection.quantity, travelerIds.size, selection.selectionId).liftTo[F]
          order <- createOrderFromSelection(
            actingUserId = membership.userId,
            planItem = planItem,
            option = option,
            travelerIds = travelerIds,
            quantity = selection.quantity,
            currentTime = currentTime
          )
          linkId <- tourGroupRepository.nextSelectionOrderLinkId
          _ <- tourGroupRepository.saveSelectionOrderLink(GroupSelectionOrderLink(linkId, selection.selectionId, order.orderId, currentTime))
        yield ()
    }

  protected def getOrCreateSelectionOrder(
      details: TourGroupDetails,
      selection: GroupPlanSelection,
      membership: TourGroupMembership,
      planItem: GroupPlanItem,
      option: GroupPlanOption,
      travelerIds: List[TravelerId],
      currentTime: Instant
  ): F[Order] =
    tourGroupRepository.findSelectionOrderLinkBySelectionId(selection.selectionId).flatMap {
      case Some(link) =>
        orderRepository.findOrderById(link.orderId).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(link.orderId)))
      case None =>
        for
          order <- createOrderFromSelection(membership.userId, planItem, option, travelerIds, selection.quantity, currentTime)
          linkId <- tourGroupRepository.nextSelectionOrderLinkId
          _ <- tourGroupRepository.saveSelectionOrderLink(GroupSelectionOrderLink(linkId, selection.selectionId, order.orderId, currentTime))
        yield order
    }

  protected def loadPayableSelection(
      details: TourGroupDetails,
      selectionId: GroupPlanSelectionId,
      actingUserId: UserId
  ): F[(GroupPlanSelection, TourGroupMembership, GroupPlanItem, GroupPlanOption, List[TravelerId])] =
    for
      selection <- details.selectionById(selectionId).liftTo[F]
      membership <- details.membershipById(selection.membershipId).liftTo[F]
      _ <- membership.ensureOwner(actingUserId).liftTo[F]
      _ <- if selection.status == GroupPlanSelectionStatus.OrganizerConfirmed then ().pure[F]
      else TourGroupError.SelectionWasNotPayable(selection.selectionId, selection.status).raiseError[F, Unit]
      planItem <- details.planItemById(selection.planItemId).liftTo[F]
      option <- details.planOptionById(selection.optionId).liftTo[F]
      travelerIds = details.selectionTravelersFor(selection.selectionId).map(_.travelerId).toList
      _ <- ensureSelectionTravelers(details, membership.membershipId, travelerIds)
      _ <- ensureSelectionQuantity(planItem.itemType, selection.quantity, travelerIds.size, selection.selectionId).liftTo[F]
    yield (selection, membership, planItem, option, travelerIds)

  protected def normalizeSelectionIds(selectionIds: List[GroupPlanSelectionId]): Either[TourGroupError, List[GroupPlanSelectionId]] =
    Either.cond(selectionIds.distinct.nonEmpty, selectionIds.distinct, TourGroupError.SelectionWasNotFound(GroupPlanSelectionId("selection")))

  protected def createOrderFromSelection(
      actingUserId: UserId,
      planItem: GroupPlanItem,
      option: GroupPlanOption,
      travelerIds: List[TravelerId],
      quantity: Int,
      currentTime: Instant
  ): F[Order] =
    option.resourceType match
      case GroupPlanOptionResourceType.Flight =>
        for
          cabinClassText <- option.resourceVariantCode.liftTo[F](TourGroupApplicationError.ResourceContextWasInvalid(option.optionId, option.resourceType))
          cabinClass <- CabinClass.create(cabinClassText).liftTo[F]
          order <- LiftIO[F].liftIO(
            flightBookingApplicationService.createFlightOrder(
              actingUserId = actingUserId,
              flightId = FlightId(option.resourceId),
              travelerIds = travelerIds,
              cabinClass = cabinClass
            )
          )
        yield order
      case GroupPlanOptionResourceType.HotelRoomType =>
        for
          checkOutAt <- planItem.endsAt.liftTo[F](TourGroupApplicationError.ResourceContextWasInvalid(option.optionId, option.resourceType))
          roomCount <- RoomCount.create(quantity).liftTo[F]
          order <- hotelBookingApplicationService.createHotelOrder(
            actingUserId = actingUserId,
            roomTypeId = RoomTypeId(option.resourceId),
            guestTravelerIds = travelerIds,
            checkInDate = planItem.scheduledAt.atZone(ZoneOffset.UTC).toLocalDate,
            checkOutDate = checkOutAt.atZone(ZoneOffset.UTC).toLocalDate,
            roomCount = roomCount
          )
        yield order
      case GroupPlanOptionResourceType.TrainJourneySeat =>
        for
          seatClassText <- option.resourceVariantCode.liftTo[F](TourGroupApplicationError.ResourceContextWasInvalid(option.optionId, option.resourceType))
          trainContext <- option.resourceContext.liftTo[F](TourGroupApplicationError.ResourceContextWasInvalid(option.optionId, option.resourceType))
          stationParts <- parseResourceContext(trainContext, option.optionId, option.resourceType)
          seatClass <- TrainSeatClass.create(seatClassText).liftTo[F]
          trainJourney <- trainBookingApplicationService.getTrainDetails(TrainId(option.resourceId))
          quote <- trainJourney.quote(
            fromStationCode = TrainStationCode.unsafe(stationParts._1),
            toStationCode = TrainStationCode.unsafe(stationParts._2),
            seatClass = seatClass,
            currentTime = currentTime
          ).liftTo[F]
          draftOrder <- orderService.createDraftOrder(actingUserId, quote.unitPrice.currency, currentTime)
          orderWithItem <- trainBookingApplicationService.addTrainItemToOrder(
            actingUserId = actingUserId,
            orderId = draftOrder.orderId,
            trainId = TrainId(option.resourceId),
            travelerIds = travelerIds,
            fromStationCode = TrainStationCode.unsafe(stationParts._1),
            toStationCode = TrainStationCode.unsafe(stationParts._2),
            seatClass = seatClass,
            seatPreference = None
          )
        yield orderWithItem
      case GroupPlanOptionResourceType.AttractionTicketType =>
        for
          attractionIdText <- option.resourceContext.liftTo[F](TourGroupApplicationError.ResourceContextWasInvalid(option.optionId, option.resourceType))
          attraction <- attractionBookingApplicationService.getAttractionDetails(AttractionId(attractionIdText))
          ticketType <- attraction.findTicketType(TicketTypeId(option.resourceId)).liftTo[F]
          draftOrder <- orderService.createDraftOrder(actingUserId, ticketType.unitPrice.currency, currentTime)
          orderWithItem <- attractionBookingApplicationService.addAttractionItemToOrder(
            actingUserId = actingUserId,
            orderId = draftOrder.orderId,
            attractionId = AttractionId(attractionIdText),
            ticketTypeId = TicketTypeId(option.resourceId),
            sessionId = None,
            travelerIds = travelerIds,
            useDate = planItem.scheduledAt.atZone(ZoneOffset.UTC).toLocalDate,
            now = currentTime
          )
        yield orderWithItem

  protected def parseResourceContext(
      resourceContext: String,
      optionId: GroupPlanOptionId,
      resourceType: GroupPlanOptionResourceType
  ): F[(String, String)] =
    resourceContext.split("\\|", 2).map(_.trim).toList match
      case from :: to :: Nil if from.nonEmpty && to.nonEmpty => (from, to).pure[F]
      case _ => TourGroupApplicationError.ResourceContextWasInvalid(optionId, resourceType).raiseError[F, (String, String)]

