package com.typesafe.travel.api.routes

import cats.MonadThrow
import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.tourgroup.domain.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

import java.time.{Instant, LocalDate}

trait TourGroupApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def tourGroupRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root / "api" / "tour-groups" =>
      for
        createTourGroupRequestDto <- request.as[CreateTourGroupRequestDto]
        startDate <- MonadThrow[F].catchNonFatal(LocalDate.parse(createTourGroupRequestDto.startDate))
        endDate <- MonadThrow[F].catchNonFatal(LocalDate.parse(createTourGroupRequestDto.endDate))
        createdAt <- currentInstantF
        detailsView <- tourGroupApplicationService.createGroup(
          organizerUserId = UserId(createTourGroupRequestDto.organizerUserId),
          title = createTourGroupRequestDto.title,
          description = createTourGroupRequestDto.description,
          destination = createTourGroupRequestDto.destination,
          startDate = startDate,
          endDate = endDate,
          capacity = createTourGroupRequestDto.capacity,
          createdAt = createdAt
        )
        response <- Created(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case GET -> Root / "api" / "tour-groups" =>
      tourGroupApplicationService.listGroups.flatMap(groups => Ok(TourGroupListResponseDto(groups.map(TourGroupSummaryResponseDto.fromView)).asJson))

    case GET -> Root / "api" / "tour-groups" / groupIdValue =>
      tourGroupApplicationService.getGroupDetails(TourGroupId(groupIdValue)).flatMap(view => Ok(TourGroupDetailsResponseDto.fromView(view).asJson))

    case request @ POST -> Root / "api" / "tour-groups" / groupIdValue / "memberships" =>
      for
        joinRequest <- request.as[JoinTourGroupRequestDto]
        joinedAt <- currentInstantF
        detailsView <- tourGroupApplicationService.joinGroup(TourGroupId(groupIdValue), UserId(joinRequest.userId), joinedAt)
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "tour-groups" / groupIdValue / "membership-travelers" =>
      for
        membershipTravelerRequest <- request.as[AddMembershipTravelerRequestDto]
        joinedAt <- currentInstantF
        detailsView <- tourGroupApplicationService.addMembershipTraveler(
          groupId = TourGroupId(groupIdValue),
          userId = UserId(membershipTravelerRequest.userId),
          travelerId = TravelerId(membershipTravelerRequest.travelerId),
          joinedAt = joinedAt
        )
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "tour-groups" / groupIdValue / "plan-items" =>
      for
        planItemRequest <- request.as[CreateGroupPlanItemRequestDto]
        scheduledAt <- MonadThrow[F].catchNonFatal(Instant.parse(planItemRequest.scheduledAt))
        endsAt <- planItemRequest.endsAt.traverse(value => MonadThrow[F].catchNonFatal(Instant.parse(value)))
        detailsView <- tourGroupApplicationService.createPlanItem(
          groupId = TourGroupId(groupIdValue),
          organizerUserId = UserId(planItemRequest.organizerUserId),
          itemType = TourGroupDtoMappers.toPlanItemType(planItemRequest.itemType),
          title = planItemRequest.title,
          description = planItemRequest.description,
          scheduledAt = scheduledAt,
          endsAt = endsAt,
          sequenceNo = planItemRequest.sequenceNo
        )
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "plan-items" / planItemIdValue / "options" =>
      for
        groupIdText <- fromEither(request.params.get("groupId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("groupId")))
        optionRequest <- request.as[CreateGroupPlanOptionRequestDto]
        detailsView <- tourGroupApplicationService.createPlanOption(
          groupId = TourGroupId(groupIdText),
          planItemId = GroupPlanItemId(planItemIdValue),
          organizerUserId = UserId(optionRequest.organizerUserId),
          resourceType = TourGroupDtoMappers.toResourceType(optionRequest.resourceType),
          resourceId = optionRequest.resourceId,
          resourceVariantCode = optionRequest.resourceVariantCode,
          resourceContext = optionRequest.resourceContext,
          label = optionRequest.label,
          description = optionRequest.description,
          defaultQuantity = optionRequest.defaultQuantity
        )
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "plan-items" / planItemIdValue / "selections" =>
      for
        groupIdText <- fromEither(request.params.get("groupId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("groupId")))
        selectionRequest <- request.as[CreateGroupPlanSelectionRequestDto]
        createdAt <- currentInstantF
        detailsView <- tourGroupApplicationService.createSelection(
          groupId = TourGroupId(groupIdText),
          actingUserId = UserId(selectionRequest.userId),
          planItemId = GroupPlanItemId(planItemIdValue),
          optionId = GroupPlanOptionId(selectionRequest.optionId),
          quantity = selectionRequest.quantity,
          travelerIds = selectionRequest.travelerIds.map(TravelerId.apply),
          createdAt = createdAt
        )
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "selections" / selectionIdValue / "submit" =>
      for
        submitRequest <- request.as[SubmitGroupPlanSelectionRequestDto]
        detailsView <- tourGroupApplicationService.submitSelection(GroupPlanSelectionId(selectionIdValue), UserId(submitRequest.userId))
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "selections" / selectionIdValue / "confirm" =>
      for
        reviewRequest <- request.as[ReviewGroupPlanSelectionRequestDto]
        confirmedAt <- currentInstantF
        detailsView <- tourGroupApplicationService.confirmSelection(
          selectionId = GroupPlanSelectionId(selectionIdValue),
          organizerUserId = UserId(reviewRequest.organizerUserId),
          reviewNote = reviewRequest.reviewNote,
          confirmedAt = confirmedAt
        )
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "selections" / selectionIdValue / "reject" =>
      for
        rejectRequest <- request.as[RejectGroupPlanSelectionRequestDto]
        rejectedAt <- currentInstantF
        detailsView <- tourGroupApplicationService.rejectSelection(
          selectionId = GroupPlanSelectionId(selectionIdValue),
          organizerUserId = UserId(rejectRequest.organizerUserId),
          reviewNote = rejectRequest.reviewNote,
          rejectedAt = rejectedAt
        )
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "selections" / selectionIdValue / "pay" =>
      for
        payRequest <- request.as[PayGroupPlanSelectionRequestDto]
        paidAt <- currentInstantF
        payResult <- tourGroupApplicationService.paySelection(
          selectionId = GroupPlanSelectionId(selectionIdValue),
          actingUserId = UserId(payRequest.userId),
          paymentMethod = OrderDtoMappers.toPaymentMethod(payRequest.paymentMethod),
          paidAt = paidAt
        )
        (detailsView, order) = payResult
        orderResponseDto <- toOrderResponseDto(order)
        response <- Ok(
          Map(
            "group" -> TourGroupDetailsResponseDto.fromView(detailsView).asJson,
            "order" -> orderResponseDto.asJson
          ).asJson
        )
      yield response

    case GET -> Root / "api" / "tour-groups" / groupIdValue / "bookings" =>
      tourGroupApplicationService.listGroupBookings(TourGroupId(groupIdValue)).flatMap { orders =>
        orders.traverse(toOrderResponseDto).flatMap(orderDtos => Ok(OrderListResponseDto(orderDtos).asJson))
      }
  }
