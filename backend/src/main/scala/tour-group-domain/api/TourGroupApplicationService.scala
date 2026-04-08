package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.identity.domain.UserRepository
import com.typesafe.travel.order.domain.{Order, OrderError, OrderService, PaymentMethod, PaymentStatus, RefundStatus, SupplierReviewStatus}
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.tourgroup.domain.*
import com.typesafe.travel.train.domain.{TrainSeatClass, TrainStationCode}
import com.typesafe.travel.traveler.domain.TravelerProfileRepository
import java.time.{Instant, LocalDate, ZoneOffset}

// TourGroup application service 负责协调 group / plan / selection / chat / order projection。
// 它驱动“计划和选择”，但真实交易仍然由 Order 聚合根承担。

final class LiveTourGroupApplicationService[F[_]: MonadThrow](
    protected val tourGroupRepository: TourGroupRepository[F],
    protected val userRepository: UserRepository[F],
    protected val travelerProfileRepository: TravelerProfileRepository[F],
    protected val orderService: OrderService[F],
    protected val orderLifecycleApplicationService: OrderLifecycleApplicationService[F],
    protected val orderRepository: com.typesafe.travel.order.domain.OrderRepository[F],
    protected val flightBookingApplicationService: FlightBookingApplicationService[F],
    protected val hotelBookingApplicationService: HotelBookingApplicationService[F],
    protected val trainBookingApplicationService: TrainBookingApplicationService[F],
    protected val attractionBookingApplicationService: AttractionBookingApplicationService[F],
    protected val chatAttachmentStorage: TourGroupChatAttachmentStorage[F]
) extends TourGroupApplicationService[F]
    with LiveTourGroupChatOperations[F]
    with LiveTourGroupSelectionSupport[F]
    with LiveTourGroupChatSupport[F]
    with LiveTourGroupChatViews[F]:

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
      // group 创建时同步补齐默认聊天配置和 public conversation，保证 detail 页始终有稳定入口。
      _ <- ensureDefaultChatSettings(group, createdAt)
      publicConversation <- ensurePublicConversation(groupId, createdAt)
      _ <- ensureConversationParticipant(publicConversation.conversationId, organizerUserId, group.organizerUserId, createdAt)
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
      // group 本体只保存 selection -> order link。
      // 这里再把真实 order 拉回来，形成 group 详情里的 booking / payment 摘要。
      linkedOrders <- loadLinkedOrders(details.selectionOrderLinks)
      memberDisplayNames <- details.memberships.toList.map(_.userId).distinct.traverse { userId =>
        userRepository.findByUserId(userId).map(user => userId -> user.map(_.userDisplayName.value).getOrElse(userId.value))
      }.map(_.toMap)
    yield TourGroupDetailsView(details, linkedOrders, memberDisplayNames)

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
      publicConversation <- ensurePublicConversation(groupId, joinedAt)
      _ <- ensureConversationParticipant(publicConversation.conversationId, userId, details.group.organizerUserId, joinedAt)
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
    paySelectionsInternal(List(selectionId), actingUserId, paymentMethod, paidAt).map { case (detailsView, paidOrders) =>
      detailsView -> paidOrders.head
    }

  override def batchConfirmSelections(
      selectionIds: List[GroupPlanSelectionId],
      organizerUserId: UserId,
      reviewNote: Option[String],
      confirmedAt: Instant
  ): F[TourGroupDetailsView] =
    batchUpdateSelections(selectionIds) { (details, selection) =>
      for
        _ <- details.group.ensureOrganizer(organizerUserId)
        updatedSelection <- selection.confirm(organizerUserId, confirmedAt, reviewNote)
      yield updatedSelection
    }.flatMap { details =>
      selectionIds.distinct.traverse_(selectionId =>
        details.selectionById(selectionId).liftTo[F].flatMap(selection => ensureSelectionOrderLinked(details, selection, confirmedAt))
      ) *> getGroupDetails(details.group.groupId)
    }

  override def batchRejectSelections(
      selectionIds: List[GroupPlanSelectionId],
      organizerUserId: UserId,
      reviewNote: String,
      rejectedAt: Instant
  ): F[TourGroupDetailsView] =
    batchUpdateSelections(selectionIds) { (details, selection) =>
      for
        _ <- details.group.ensureOrganizer(organizerUserId)
        updatedSelection <- selection.reject(organizerUserId, rejectedAt, reviewNote)
      yield updatedSelection
    }.flatMap(details => getGroupDetails(details.group.groupId))

  override def batchPaySelections(
      selectionIds: List[GroupPlanSelectionId],
      actingUserId: UserId,
      paymentMethod: PaymentMethod,
      paidAt: Instant
  ): F[(TourGroupDetailsView, List[Order])] =
    paySelectionsInternal(selectionIds, actingUserId, paymentMethod, paidAt)

  private def paySelectionsInternal(
      selectionIds: List[GroupPlanSelectionId],
      actingUserId: UserId,
      paymentMethod: PaymentMethod,
      paidAt: Instant
  ): F[(TourGroupDetailsView, List[Order])] =
    // batch pay 只是把多个 selection 统一结算；
    // 内部仍然转换成真实 Order，因此 group 不会变成新的交易聚合根。
    for
      normalizedSelectionIds <- normalizeSelectionIds(selectionIds).liftTo[F]
      anchorDetails <- loadGroupDetailsBySelection(normalizedSelectionIds.head)
      validatedSelections <- normalizedSelectionIds.traverse(selectionId => loadPayableSelection(anchorDetails, selectionId, actingUserId))
      paidOrders <- validatedSelections.traverse { case (selection, membership, planItem, option, travelerIds) =>
        for
          order <- getOrCreateSelectionOrder(anchorDetails, selection, membership, planItem, option, travelerIds, paidAt)
          paidOrder <- orderLifecycleApplicationService.payOrder(order.orderId, paymentMethod, paymentSucceeded = true, currentTime = paidAt)
          convertedSelection <- selection.markConvertedToOrder.liftTo[F]
          _ <- tourGroupRepository.saveSelection(convertedSelection)
        yield paidOrder
      }
      refreshed <- getGroupDetails(anchorDetails.group.groupId)
    yield refreshed -> paidOrders

  override def listGroupBookings(groupId: TourGroupId): F[List[Order]] =
    loadGroupDetails(groupId).flatMap(details => loadLinkedOrders(details.selectionOrderLinks).map(_.values.toList.sortBy(_.createdAt.toEpochMilli).reverse))

