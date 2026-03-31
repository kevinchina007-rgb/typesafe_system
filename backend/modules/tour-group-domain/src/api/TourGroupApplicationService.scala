package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.order.domain.{Order, OrderError, OrderService, PaymentMethod}
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.tourgroup.domain.*
import com.typesafe.travel.train.domain.{TrainSeatClass, TrainStationCode}
import com.typesafe.travel.traveler.domain.TravelerProfileRepository

import java.time.{Instant, LocalDate, ZoneOffset}

final case class TourGroupDetailsView(
    details: TourGroupDetails,
    linkedOrders: Map[GroupPlanSelectionId, Order]
)

enum TourGroupApplicationError(val message: String) extends DomainError:
  case ResourceContextWasInvalid(optionId: GroupPlanOptionId, resourceType: GroupPlanOptionResourceType)
      extends TourGroupApplicationError(
        s"Option '${optionId.value}' has invalid resource context for resource type $resourceType"
      )
  case MembershipTravelerWasNotFound(travelerId: TravelerId)
      extends TourGroupApplicationError(s"Traveler '${travelerId.value}' was not found")
  case MembershipTravelerWasNotOwnedByUser(travelerId: TravelerId, userId: UserId)
      extends TourGroupApplicationError(s"Traveler '${travelerId.value}' is not owned by user '${userId.value}'")
  case SelectionOrderCreationFailed(selectionId: GroupPlanSelectionId)
      extends TourGroupApplicationError(s"Selection '${selectionId.value}' could not be converted into an order")

trait TourGroupApplicationService[F[_]]:
  def createGroup(
      organizerUserId: UserId,
      title: String,
      description: String,
      destination: String,
      startDate: LocalDate,
      endDate: LocalDate,
      capacity: Int,
      createdAt: Instant
  ): F[TourGroupDetailsView]
  def listGroups: F[List[TourGroupDetailsView]]
  def getGroupDetails(groupId: TourGroupId): F[TourGroupDetailsView]
  def joinGroup(groupId: TourGroupId, userId: UserId, joinedAt: Instant): F[TourGroupDetailsView]
  def addMembershipTraveler(groupId: TourGroupId, userId: UserId, travelerId: TravelerId, joinedAt: Instant): F[TourGroupDetailsView]
  def createPlanItem(
      groupId: TourGroupId,
      organizerUserId: UserId,
      itemType: GroupPlanItemType,
      title: String,
      description: String,
      scheduledAt: Instant,
      endsAt: Option[Instant],
      sequenceNo: Int
  ): F[TourGroupDetailsView]
  def createPlanOption(
      groupId: TourGroupId,
      planItemId: GroupPlanItemId,
      organizerUserId: UserId,
      resourceType: GroupPlanOptionResourceType,
      resourceId: String,
      resourceVariantCode: Option[String],
      resourceContext: Option[String],
      label: String,
      description: String,
      defaultQuantity: Int
  ): F[TourGroupDetailsView]
  def createSelection(
      groupId: TourGroupId,
      actingUserId: UserId,
      planItemId: GroupPlanItemId,
      optionId: GroupPlanOptionId,
      quantity: Int,
      travelerIds: List[TravelerId],
      createdAt: Instant
  ): F[TourGroupDetailsView]
  def submitSelection(selectionId: GroupPlanSelectionId, actingUserId: UserId): F[TourGroupDetailsView]
  def confirmSelection(selectionId: GroupPlanSelectionId, organizerUserId: UserId, reviewNote: Option[String], confirmedAt: Instant): F[TourGroupDetailsView]
  def rejectSelection(selectionId: GroupPlanSelectionId, organizerUserId: UserId, reviewNote: String, rejectedAt: Instant): F[TourGroupDetailsView]
  def paySelection(selectionId: GroupPlanSelectionId, actingUserId: UserId, paymentMethod: PaymentMethod, paidAt: Instant): F[(TourGroupDetailsView, Order)]
  def listGroupBookings(groupId: TourGroupId): F[List[Order]]

final class LiveTourGroupApplicationService[F[_]: MonadThrow](
    tourGroupRepository: TourGroupRepository[F],
    travelerProfileRepository: TravelerProfileRepository[F],
    orderService: OrderService[F],
    orderLifecycleApplicationService: OrderLifecycleApplicationService[F],
    orderRepository: com.typesafe.travel.order.domain.OrderRepository[F],
    flightBookingApplicationService: FlightBookingApplicationService[F],
    hotelBookingApplicationService: HotelBookingApplicationService[F],
    trainBookingApplicationService: TrainBookingApplicationService[F],
    attractionBookingApplicationService: AttractionBookingApplicationService[F]
) extends TourGroupApplicationService[F]:

  override def createGroup(
      organizerUserId: UserId,
      title: String,
      description: String,
      destination: String,
      startDate: LocalDate,
      endDate: LocalDate,
      capacity: Int,
      createdAt: Instant
  ): F[TourGroupDetailsView] =
    for
      groupId <- tourGroupRepository.nextGroupId
      membershipId <- tourGroupRepository.nextMembershipId
      group <- createTourGroup(groupId, organizerUserId, title, description, destination, startDate, endDate, capacity, createdAt).liftTo[F]
      organizerMembership = createOrganizerMembership(membershipId, groupId, organizerUserId, createdAt)
      _ <- tourGroupRepository.saveGroup(group)
      _ <- tourGroupRepository.saveMembership(organizerMembership)
      detailsView <- getGroupDetails(groupId)
    yield detailsView

  override def listGroups: F[List[TourGroupDetailsView]] =
    for
      groups <- tourGroupRepository.listGroups
      details <- groups.sortBy(_.createdAt.toEpochMilli).reverse.traverse(group => getGroupDetails(group.groupId))
    yield details

  override def getGroupDetails(groupId: TourGroupId): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetails(groupId)
      linkedOrders <- loadLinkedOrders(details.selectionOrderLinks)
    yield TourGroupDetailsView(details, linkedOrders)

  override def joinGroup(groupId: TourGroupId, userId: UserId, joinedAt: Instant): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetails(groupId)
      _ <- details.group.ensureOpen.liftTo[F]
      _ <- details.memberships.find(m => m.userId == userId && m.status == TourGroupMembershipStatus.Active) match
        case Some(_) => TourGroupError.ActiveMembershipAlreadyExists(groupId, userId).raiseError[F, Unit]
        case None    => ().pure[F]
      membershipId <- tourGroupRepository.nextMembershipId
      membership = createMemberMembership(membershipId, groupId, userId, joinedAt)
      _ <- tourGroupRepository.saveMembership(membership)
      refreshed <- getGroupDetails(groupId)
    yield refreshed

  override def addMembershipTraveler(groupId: TourGroupId, userId: UserId, travelerId: TravelerId, joinedAt: Instant): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetails(groupId)
      membership <- details.memberships.find(m => m.userId == userId && m.status == TourGroupMembershipStatus.Active).liftTo[F](TourGroupError.MembershipWasNotFound(TourGroupMembershipId(s"membership-$userId")))
      _ <- ensureTravelerBelongsToUser(userId, travelerId)
      _ <- if details.membershipTravelers.exists(mt => mt.membershipId == membership.membershipId && mt.travelerId == travelerId && mt.status == TourGroupMembershipTravelerStatus.Active) then
        TourGroupError.MembershipTravelerAlreadyExists(membership.membershipId, travelerId).raiseError[F, Unit]
      else ().pure[F]
      projectedUsedCapacity = details.usedCapacity + 1
      _ <- if projectedUsedCapacity > details.group.capacity then
        TourGroupError.GroupCapacityWasExceeded(groupId, details.group.capacity, projectedUsedCapacity).raiseError[F, Unit]
      else ().pure[F]
      membershipTravelerId <- tourGroupRepository.nextMembershipTravelerId
      membershipTraveler = TourGroupMembershipTraveler(membershipTravelerId, membership.membershipId, travelerId, joinedAt, TourGroupMembershipTravelerStatus.Active)
      _ <- tourGroupRepository.saveMembershipTraveler(membershipTraveler)
      refreshed <- getGroupDetails(groupId)
    yield refreshed

  override def createPlanItem(
      groupId: TourGroupId,
      organizerUserId: UserId,
      itemType: GroupPlanItemType,
      title: String,
      description: String,
      scheduledAt: Instant,
      endsAt: Option[Instant],
      sequenceNo: Int
  ): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetails(groupId)
      _ <- details.group.ensureOrganizer(organizerUserId).liftTo[F]
      planItemId <- tourGroupRepository.nextPlanItemId
      planItem <- createGroupPlanItem(planItemId, groupId, itemType, title, description, scheduledAt, endsAt, sequenceNo).liftTo[F]
      _ <- tourGroupRepository.savePlanItem(planItem)
      refreshed <- getGroupDetails(groupId)
    yield refreshed

  override def createPlanOption(
      groupId: TourGroupId,
      planItemId: GroupPlanItemId,
      organizerUserId: UserId,
      resourceType: GroupPlanOptionResourceType,
      resourceId: String,
      resourceVariantCode: Option[String],
      resourceContext: Option[String],
      label: String,
      description: String,
      defaultQuantity: Int
  ): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetails(groupId)
      _ <- details.group.ensureOrganizer(organizerUserId).liftTo[F]
      planItem <- details.planItemById(planItemId).liftTo[F]
      _ <- ensureOptionResourceTypeMatchesPlanItem(planItem, resourceType).liftTo[F]
      optionId <- tourGroupRepository.nextPlanOptionId
      option <- createGroupPlanOption(optionId, planItemId, resourceType, resourceId, resourceVariantCode, resourceContext, label, description, defaultQuantity).liftTo[F]
      _ <- tourGroupRepository.savePlanOption(option)
      refreshed <- getGroupDetails(groupId)
    yield refreshed

  override def createSelection(
      groupId: TourGroupId,
      actingUserId: UserId,
      planItemId: GroupPlanItemId,
      optionId: GroupPlanOptionId,
      quantity: Int,
      travelerIds: List[TravelerId],
      createdAt: Instant
  ): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetails(groupId)
      membership <- details.memberships.find(m => m.userId == actingUserId && m.status == TourGroupMembershipStatus.Active).liftTo[F](TourGroupError.MembershipWasNotFound(TourGroupMembershipId(s"membership-$actingUserId")))
      planItem <- details.planItemById(planItemId).liftTo[F]
      option <- details.planOptionById(optionId).liftTo[F]
      _ <- Either.cond(option.planItemId == planItemId, (), TourGroupError.PlanOptionDidNotBelongToPlanItem(option.optionId, planItemId)).liftTo[F]
      _ <- ensureSelectionTravelers(details, membership.membershipId, travelerIds)
      _ <- ensureSelectionQuantity(planItem.itemType, quantity, travelerIds.size, GroupPlanSelectionId("pending-selection")).liftTo[F]
      selectionId <- tourGroupRepository.nextSelectionId
      selection <- createGroupPlanSelection(selectionId, groupId, planItemId, optionId, membership.membershipId, quantity, createdAt).liftTo[F]
      _ <- tourGroupRepository.saveSelection(selection)
      _ <- travelerIds.distinct.traverse_ { travelerId =>
        tourGroupRepository.nextSelectionTravelerId.flatMap { selectionTravelerId =>
          tourGroupRepository.saveSelectionTraveler(GroupPlanSelectionTraveler(selectionTravelerId, selection.selectionId, travelerId))
        }
      }
      refreshed <- getGroupDetails(groupId)
    yield refreshed

  override def submitSelection(selectionId: GroupPlanSelectionId, actingUserId: UserId): F[TourGroupDetailsView] =
    mutateSelection(selectionId) { (details, selection) =>
      for
        membership <- details.membershipById(selection.membershipId)
        _ <- membership.ensureOwner(actingUserId)
        selectionTravelers = details.selectionTravelersFor(selection.selectionId)
        _ <- if selectionTravelers.nonEmpty then Right(()) else Left(TourGroupError.SelectionTravelerWasEmpty(selection.planItemId))
        updatedSelection <- selection.submit
      yield updatedSelection
    }

  override def confirmSelection(selectionId: GroupPlanSelectionId, organizerUserId: UserId, reviewNote: Option[String], confirmedAt: Instant): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetailsBySelection(selectionId)
      selection <- details.selectionById(selectionId).liftTo[F]
      _ <- details.group.ensureOrganizer(organizerUserId).liftTo[F]
      updatedSelection <- selection.confirm(organizerUserId, confirmedAt, reviewNote).liftTo[F]
      _ <- tourGroupRepository.saveSelection(updatedSelection)
      _ <- ensureSelectionOrderLinked(details, updatedSelection, confirmedAt)
      refreshed <- getGroupDetails(details.group.groupId)
    yield refreshed

  override def rejectSelection(selectionId: GroupPlanSelectionId, organizerUserId: UserId, reviewNote: String, rejectedAt: Instant): F[TourGroupDetailsView] =
    mutateSelection(selectionId) { (details, selection) =>
      for
        _ <- details.group.ensureOrganizer(organizerUserId)
        updatedSelection <- selection.reject(organizerUserId, rejectedAt, reviewNote)
      yield updatedSelection
    }

  override def paySelection(selectionId: GroupPlanSelectionId, actingUserId: UserId, paymentMethod: PaymentMethod, paidAt: Instant): F[(TourGroupDetailsView, Order)] =
    for
      details <- loadGroupDetailsBySelection(selectionId)
      selection <- details.selectionById(selectionId).liftTo[F]
      membership <- details.membershipById(selection.membershipId).liftTo[F]
      _ <- membership.ensureOwner(actingUserId).liftTo[F]
      _ <- if selection.status == GroupPlanSelectionStatus.OrganizerConfirmed then ().pure[F]
      else TourGroupError.SelectionWasNotPayable(selection.selectionId, selection.status).raiseError[F, Unit]
      existingLink <- tourGroupRepository.findSelectionOrderLinkBySelectionId(selection.selectionId)
      _ <- existingLink match
        case Some(link) => TourGroupError.SelectionWasAlreadyLinked(selection.selectionId, link.orderId).raiseError[F, Unit]
        case None       => ().pure[F]
      planItem <- details.planItemById(selection.planItemId).liftTo[F]
      option <- details.planOptionById(selection.optionId).liftTo[F]
      selectionTravelers = details.selectionTravelersFor(selection.selectionId).map(_.travelerId).toList
      _ <- ensureSelectionTravelers(details, membership.membershipId, selectionTravelers)
      _ <- ensureSelectionQuantity(planItem.itemType, selection.quantity, selectionTravelers.size, selection.selectionId).liftTo[F]
      order <- createOrderFromSelection(actingUserId, planItem, option, selectionTravelers, selection.quantity, paidAt)
      paidOrder <- orderLifecycleApplicationService.payOrder(order.orderId, paymentMethod, paymentSucceeded = true, currentTime = paidAt)
      linkId <- tourGroupRepository.nextSelectionOrderLinkId
      _ <- tourGroupRepository.saveSelectionOrderLink(GroupSelectionOrderLink(linkId, selection.selectionId, paidOrder.orderId, paidAt))
      convertedSelection <- selection.markConvertedToOrder.liftTo[F]
      _ <- tourGroupRepository.saveSelection(convertedSelection)
      refreshed <- getGroupDetails(details.group.groupId)
    yield refreshed -> paidOrder

  override def listGroupBookings(groupId: TourGroupId): F[List[Order]] =
    loadGroupDetails(groupId).flatMap(details => loadLinkedOrders(details.selectionOrderLinks).map(_.values.toList.sortBy(_.createdAt.toEpochMilli).reverse))

  private def mutateSelection(
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

  private def loadGroupDetails(groupId: TourGroupId): F[TourGroupDetails] =
    tourGroupRepository.findGroupDetails(groupId).flatMap(_.liftTo[F](TourGroupError.GroupWasNotFound(groupId)))

  private def loadGroupDetailsBySelection(selectionId: GroupPlanSelectionId): F[TourGroupDetails] =
    tourGroupRepository
      .findSelectionById(selectionId)
      .flatMap(_.liftTo[F](TourGroupError.SelectionWasNotFound(selectionId)))
      .flatMap(selection => loadGroupDetails(selection.groupId))

  private def loadLinkedOrders(links: Vector[GroupSelectionOrderLink]): F[Map[GroupPlanSelectionId, Order]] =
    links.toList.traverse { link =>
      orderRepository.findOrderById(link.orderId).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(link.orderId))).map(order => link.selectionId -> order)
    }.map(_.toMap)

  private def ensureTravelerBelongsToUser(userId: UserId, travelerId: TravelerId): F[Unit] =
    travelerProfileRepository.findTravelerProfileById(travelerId).flatMap {
      case None => TourGroupApplicationError.MembershipTravelerWasNotFound(travelerId).raiseError[F, Unit]
      case Some(travelerProfile) if travelerProfile.ownerUserId == userId => ().pure[F]
      case Some(_) => TourGroupApplicationError.MembershipTravelerWasNotOwnedByUser(travelerId, userId).raiseError[F, Unit]
    }

  private def ensureSelectionTravelers(
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

  private def ensureSelectionQuantity(
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

  private def ensureOptionResourceTypeMatchesPlanItem(
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

  private def ensureSelectionOrderLinked(
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

  private def createOrderFromSelection(
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
          order <- flightBookingApplicationService.createFlightOrder(
            actingUserId = actingUserId,
            flightId = FlightId(option.resourceId),
            travelerIds = travelerIds,
            cabinClass = cabinClass
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
          currency = quote.unitPrice.currency
            draftOrder <- orderService.createDraftOrder(actingUserId, currency, currentTime)
          orderWithItem <- trainBookingApplicationService.addTrainItemToOrder(
            actingUserId = actingUserId,
            orderId = draftOrder.orderId,
            trainId = TrainId(option.resourceId),
            travelerIds = travelerIds,
            fromStationCode = TrainStationCode.unsafe(stationParts._1),
            toStationCode = TrainStationCode.unsafe(stationParts._2),
            seatClass = seatClass
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
            travelerIds = travelerIds,
            useDate = planItem.scheduledAt.atZone(ZoneOffset.UTC).toLocalDate,
            now = currentTime
          )
        yield orderWithItem

  private def parseResourceContext(
      resourceContext: String,
      optionId: GroupPlanOptionId,
      resourceType: GroupPlanOptionResourceType
  ): F[(String, String)] =
    resourceContext.split("\\|", 2).map(_.trim).toList match
      case from :: to :: Nil if from.nonEmpty && to.nonEmpty => (from, to).pure[F]
      case _ => TourGroupApplicationError.ResourceContextWasInvalid(optionId, resourceType).raiseError[F, (String, String)]

