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
import org.http4s.headers
import org.http4s.multipart.Multipart

import java.time.{Instant, LocalDate}

trait TourGroupApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  private given EntityDecoder[F, UpdateTourGroupChatSettingsRequestDto] = jsonOf[F, UpdateTourGroupChatSettingsRequestDto]
  private given EntityDecoder[F, CreateDirectConversationRequestDto] = jsonOf[F, CreateDirectConversationRequestDto]
  private given EntityDecoder[F, SendTourGroupMessageRequestDto] = jsonOf[F, SendTourGroupMessageRequestDto]
  private given EntityDecoder[F, EditTourGroupMessageRequestDto] = jsonOf[F, EditTourGroupMessageRequestDto]
  private given EntityDecoder[F, ReactTourGroupMessageRequestDto] = jsonOf[F, ReactTourGroupMessageRequestDto]
  private given EntityDecoder[F, UpdateConversationMuteRequestDto] = jsonOf[F, UpdateConversationMuteRequestDto]
  private given EntityDecoder[F, UpdateConversationArchiveRequestDto] = jsonOf[F, UpdateConversationArchiveRequestDto]

  protected final def tourGroupRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root / "api" / "tour-groups" =>
      for
        currentUserId <- requireCurrentUserId(request)
        createTourGroupRequestDto <- request.as[CreateTourGroupRequestDto]
        startDate <- MonadThrow[F].catchNonFatal(LocalDate.parse(createTourGroupRequestDto.startDate))
        endDate <- MonadThrow[F].catchNonFatal(LocalDate.parse(createTourGroupRequestDto.endDate))
        createdAt <- currentInstantF
        detailsView <- tourGroupApplicationService.createGroup(
          organizerUserId = currentUserId,
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
        currentUserId <- requireCurrentUserId(request)
        joinRequest <- request.as[JoinTourGroupRequestDto]
        joinedAt <- currentInstantF
        detailsView <- tourGroupApplicationService.joinGroup(TourGroupId(groupIdValue), currentUserId, joinedAt)
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "tour-groups" / groupIdValue / "membership-travelers" =>
      for
        currentUserId <- requireCurrentUserId(request)
        membershipTravelerRequest <- request.as[AddMembershipTravelerRequestDto]
        joinedAt <- currentInstantF
        detailsView <- tourGroupApplicationService.addMembershipTraveler(
          groupId = TourGroupId(groupIdValue),
          userId = currentUserId,
          travelerId = TravelerId(membershipTravelerRequest.travelerId),
          joinedAt = joinedAt
        )
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "tour-groups" / groupIdValue / "plan-items" =>
      for
        currentUserId <- requireCurrentUserId(request)
        planItemRequest <- request.as[CreateGroupPlanItemRequestDto]
        scheduledAt <- MonadThrow[F].catchNonFatal(Instant.parse(planItemRequest.scheduledAt))
        endsAt <- planItemRequest.endsAt.traverse(value => MonadThrow[F].catchNonFatal(Instant.parse(value)))
        detailsView <- tourGroupApplicationService.createPlanItem(
          groupId = TourGroupId(groupIdValue),
          organizerUserId = currentUserId,
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
        currentUserId <- requireCurrentUserId(request)
        groupIdText <- fromEither(request.params.get("groupId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("groupId")))
        optionRequest <- request.as[CreateGroupPlanOptionRequestDto]
        detailsView <- tourGroupApplicationService.createPlanOption(
          groupId = TourGroupId(groupIdText),
          planItemId = GroupPlanItemId(planItemIdValue),
          organizerUserId = currentUserId,
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
        currentUserId <- requireCurrentUserId(request)
        groupIdText <- fromEither(request.params.get("groupId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("groupId")))
        selectionRequest <- request.as[CreateGroupPlanSelectionRequestDto]
        createdAt <- currentInstantF
        detailsView <- tourGroupApplicationService.createSelection(
          groupId = TourGroupId(groupIdText),
          actingUserId = currentUserId,
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
        currentUserId <- requireCurrentUserId(request)
        submitRequest <- request.as[SubmitGroupPlanSelectionRequestDto]
        detailsView <- tourGroupApplicationService.submitSelection(GroupPlanSelectionId(selectionIdValue), currentUserId)
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "selections" / selectionIdValue / "confirm" =>
      for
        currentUserId <- requireCurrentUserId(request)
        reviewRequest <- request.as[ReviewGroupPlanSelectionRequestDto]
        confirmedAt <- currentInstantF
        detailsView <- tourGroupApplicationService.confirmSelection(
          selectionId = GroupPlanSelectionId(selectionIdValue),
          organizerUserId = currentUserId,
          reviewNote = reviewRequest.reviewNote,
          confirmedAt = confirmedAt
        )
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "selections" / selectionIdValue / "reject" =>
      for
        currentUserId <- requireCurrentUserId(request)
        rejectRequest <- request.as[RejectGroupPlanSelectionRequestDto]
        rejectedAt <- currentInstantF
        detailsView <- tourGroupApplicationService.rejectSelection(
          selectionId = GroupPlanSelectionId(selectionIdValue),
          organizerUserId = currentUserId,
          reviewNote = rejectRequest.reviewNote,
          rejectedAt = rejectedAt
        )
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "selections" / selectionIdValue / "pay" =>
      for
        currentUserId <- requireCurrentUserId(request)
        payRequest <- request.as[PayGroupPlanSelectionRequestDto]
        paidAt <- currentInstantF
        payResult <- tourGroupApplicationService.paySelection(
          selectionId = GroupPlanSelectionId(selectionIdValue),
          actingUserId = currentUserId,
          paymentMethod = OrderDtoMappers.toPaymentMethod(payRequest.paymentMethod),
          paidAt = paidAt
        )
        (detailsView, order) = payResult
        orderResponseDto <- toOrderResponseDto(order)
        response <- Ok(TourGroupBatchPayResponseDto(TourGroupDetailsResponseDto.fromView(detailsView), List(orderResponseDto)).asJson)
      yield response

    case request @ POST -> Root / "api" / "selections" / "batch-pay" =>
      for
        currentUserId <- requireCurrentUserId(request)
        payRequest <- request.as[BatchPayGroupPlanSelectionsRequestDto]
        paidAt <- currentInstantF
        payResult <- tourGroupApplicationService.batchPaySelections(
          selectionIds = payRequest.selectionIds.map(GroupPlanSelectionId.apply),
          actingUserId = currentUserId,
          paymentMethod = OrderDtoMappers.toPaymentMethod(payRequest.paymentMethod),
          paidAt = paidAt
        )
        (detailsView, orders) = payResult
        orderResponseDtos <- orders.traverse(toOrderResponseDto)
        response <- Ok(TourGroupBatchPayResponseDto(TourGroupDetailsResponseDto.fromView(detailsView), orderResponseDtos).asJson)
      yield response

    case request @ POST -> Root / "api" / "selections" / "batch-confirm" =>
      for
        currentUserId <- requireCurrentUserId(request)
        reviewRequest <- request.as[BatchReviewGroupPlanSelectionsRequestDto]
        confirmedAt <- currentInstantF
        detailsView <- tourGroupApplicationService.batchConfirmSelections(
          selectionIds = reviewRequest.selectionIds.map(GroupPlanSelectionId.apply),
          organizerUserId = currentUserId,
          reviewNote = reviewRequest.reviewNote,
          confirmedAt = confirmedAt
        )
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case request @ POST -> Root / "api" / "selections" / "batch-reject" =>
      for
        currentUserId <- requireCurrentUserId(request)
        rejectRequest <- request.as[BatchRejectGroupPlanSelectionsRequestDto]
        rejectedAt <- currentInstantF
        detailsView <- tourGroupApplicationService.batchRejectSelections(
          selectionIds = rejectRequest.selectionIds.map(GroupPlanSelectionId.apply),
          organizerUserId = currentUserId,
          reviewNote = rejectRequest.reviewNote,
          rejectedAt = rejectedAt
        )
        response <- Ok(TourGroupDetailsResponseDto.fromView(detailsView).asJson)
      yield response

    case GET -> Root / "api" / "tour-groups" / groupIdValue / "bookings" =>
      tourGroupApplicationService.listGroupBookings(TourGroupId(groupIdValue)).flatMap { orders =>
        orders.traverse(toOrderResponseDto).flatMap(orderDtos => Ok(OrderListResponseDto(orderDtos).asJson))
      }

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "chat-settings" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        settingsView <- tourGroupApplicationService.getChatSettings(TourGroupId(groupIdValue), currentUserId, currentTime)
        response <- Ok(TourGroupChatSettingsResponseDto.fromView(settingsView).asJson)
      yield response

    case request @ PATCH -> Root / "api" / "tour-groups" / groupIdValue / "chat-settings" =>
      for
        currentUserId <- requireCurrentUserId(request)
        settingsRequest <- request.as[UpdateTourGroupChatSettingsRequestDto]
        updatedAt <- currentInstantF
        settingsView <- tourGroupApplicationService.updateChatSettings(
          groupId = TourGroupId(groupIdValue),
          organizerUserId = currentUserId,
          allowMemberDirectChat = settingsRequest.allowMemberDirectChat,
          updatedAt = updatedAt
        )
        response <- Ok(TourGroupChatSettingsResponseDto.fromView(settingsView).asJson)
      yield response

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "conversations" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        conversations <- tourGroupApplicationService.listConversations(TourGroupId(groupIdValue), currentUserId, currentTime)
        response <- Ok(TourGroupConversationListResponseDto.fromView(conversations).asJson)
      yield response

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "chat" / "conversation" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        conversations <- tourGroupApplicationService.listConversations(TourGroupId(groupIdValue), currentUserId, currentTime)
        response <- Ok(TourGroupConversationListResponseDto.fromView(conversations).asJson)
      yield response

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "chat" / "messages" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        messageViews <- tourGroupApplicationService.listGroupChatMessages(TourGroupId(groupIdValue), currentUserId, currentTime)
        response <- Ok(TourGroupMessageListResponseDto(messageViews.map(TourGroupMessageResponseDto.fromView)).asJson)
      yield response

    case request @ POST -> Root / "api" / "tour-groups" / groupIdValue / "chat" / "messages" =>
      for
        currentUserId <- requireCurrentUserId(request)
        messageRequest <- request.as[SendTourGroupMessageRequestDto]
        createdAt <- currentInstantF
        messageViews <- tourGroupApplicationService.sendGroupChatMessage(TourGroupId(groupIdValue), currentUserId, messageRequest.content, createdAt)
        response <- Ok(TourGroupMessageListResponseDto(messageViews.map(TourGroupMessageResponseDto.fromView)).asJson)
      yield response

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "chat" / "search" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        query <- fromEither(request.params.get("q").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("q")))
        results <- tourGroupApplicationService.searchMessages(TourGroupId(groupIdValue), currentUserId, query, currentTime)
        response <- Ok(TourGroupMessageSearchResponseDto(results.map(TourGroupMessageSearchResultResponseDto.fromView)).asJson)
      yield response

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "chat" / "conversations" / "search" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        query <- fromEither(request.params.get("q").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("q")))
        results <- tourGroupApplicationService.searchConversations(TourGroupId(groupIdValue), currentUserId, query, currentTime)
        response <- Ok(TourGroupConversationListResponseDto(results.map(TourGroupConversationSummaryResponseDto.fromView), None).asJson)
      yield response

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "direct-conversations" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        conversations <- tourGroupApplicationService.listDirectConversations(TourGroupId(groupIdValue), currentUserId, currentTime)
        response <- Ok(TourGroupConversationListResponseDto(conversations.map(TourGroupConversationSummaryResponseDto.fromView), None).asJson)
      yield response

    case request @ POST -> Root / "api" / "tour-groups" / groupIdValue / "direct-conversations" =>
      for
        currentUserId <- requireCurrentUserId(request)
        conversationRequest <- request.as[CreateDirectConversationRequestDto]
        currentTime <- currentInstantF
        conversation <- tourGroupApplicationService.getOrCreateDirectConversation(
          groupId = TourGroupId(groupIdValue),
          actingUserId = currentUserId,
          targetUserId = UserId(conversationRequest.targetUserId),
          currentTime = currentTime
        )
        response <- Ok(TourGroupConversationSummaryResponseDto.fromView(conversation).asJson)
      yield response

    case request @ POST -> Root / "api" / "conversations" / conversationIdValue / "attachments" =>
      for
        currentUserId <- requireCurrentUserId(request)
        multipartPayload <- request.as[Multipart[F]]
        filePart <- multipartPayload.parts.find(_.name.contains("attachment")).liftTo[F](SharedValidationError.RequiredFieldWasEmpty("attachment"))
        fileName <- filePart.filename.liftTo[F](SharedValidationError.RequiredFieldWasEmpty("attachment.filename"))
        mimeType =
          filePart.headers
            .get[headers.`Content-Type`]
            .map(header => s"${header.mediaType.mainType}/${header.mediaType.subType}")
            .getOrElse("application/octet-stream")
        fileBytes <- filePart.body.compile.to(Array)
        groupIdValue <- fromEither(request.params.get("groupId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("groupId")))
        currentTime <- currentInstantF
        uploaded <- tourGroupApplicationService.uploadConversationAttachment(TourGroupId(groupIdValue), currentUserId, fileName, mimeType, fileBytes, currentTime)
        response <- Created(TourGroupUploadedAttachmentResponseDto.fromView(uploaded).asJson)
      yield response

    case request @ GET -> Root / "api" / "conversations" / conversationIdValue / "messages" =>
      for
        currentUserId <- requireCurrentUserId(request)
        messages <- tourGroupApplicationService.getConversationMessages(TourGroupConversationId(conversationIdValue), currentUserId)
        response <- Ok(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)).asJson)
      yield response

    case request @ POST -> Root / "api" / "conversations" / conversationIdValue / "messages" =>
      for
        currentUserId <- requireCurrentUserId(request)
        messageRequest <- request.as[SendTourGroupMessageRequestDto]
        createdAt <- currentInstantF
        messages <- tourGroupApplicationService.sendConversationMessage(
          conversationId = TourGroupConversationId(conversationIdValue),
          actingUserId = currentUserId,
          messageType = messageRequest.messageType.map(TourGroupDtoMappers.toMessageType).getOrElse(TourGroupMessageType.Text),
          content = messageRequest.content,
          replyToMessageId = messageRequest.replyToMessageId.map(TourGroupMessageId.apply),
          attachmentRefs = messageRequest.attachments.map(TourGroupUploadedAttachmentResponseDto.toView),
          createdAt = createdAt
        )
        response <- Ok(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)).asJson)
      yield response

    case request @ POST -> Root / "api" / "conversations" / conversationIdValue / "read" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        summary <- tourGroupApplicationService.markConversationRead(TourGroupConversationId(conversationIdValue), currentUserId, currentTime)
        response <- Ok(TourGroupConversationSummaryResponseDto.fromView(summary).asJson)
      yield response

    case request @ GET -> Root / "api" / "direct-conversations" / conversationIdValue / "messages" =>
      for
        currentUserId <- requireCurrentUserId(request)
        messageViews <- tourGroupApplicationService.listDirectConversationMessages(TourGroupConversationId(conversationIdValue), currentUserId)
        response <- Ok(TourGroupMessageListResponseDto(messageViews.map(TourGroupMessageResponseDto.fromView)).asJson)
      yield response

    case request @ PATCH -> Root / "api" / "direct-conversations" / conversationIdValue / "mute" =>
      for
        currentUserId <- requireCurrentUserId(request)
        muteRequest <- request.as[UpdateConversationMuteRequestDto]
        currentTime <- currentInstantF
        summary <- tourGroupApplicationService.updateConversationMuteState(TourGroupConversationId(conversationIdValue), currentUserId, muteRequest.muted, currentTime)
        response <- Ok(TourGroupConversationSummaryResponseDto.fromView(summary).asJson)
      yield response

    case request @ PATCH -> Root / "api" / "direct-conversations" / conversationIdValue / "archive" =>
      for
        currentUserId <- requireCurrentUserId(request)
        archiveRequest <- request.as[UpdateConversationArchiveRequestDto]
        currentTime <- currentInstantF
        summary <- tourGroupApplicationService.updateConversationArchiveState(TourGroupConversationId(conversationIdValue), currentUserId, archiveRequest.archived, currentTime)
        response <- Ok(TourGroupConversationSummaryResponseDto.fromView(summary).asJson)
      yield response

    case request @ POST -> Root / "api" / "direct-conversations" / conversationIdValue / "messages" =>
      for
        currentUserId <- requireCurrentUserId(request)
        messageRequest <- request.as[SendTourGroupMessageRequestDto]
        createdAt <- currentInstantF
        messageViews <- tourGroupApplicationService.sendDirectConversationMessage(
          conversationId = TourGroupConversationId(conversationIdValue),
          actingUserId = currentUserId,
          content = messageRequest.content,
          createdAt = createdAt
        )
        response <- Ok(TourGroupMessageListResponseDto(messageViews.map(TourGroupMessageResponseDto.fromView)).asJson)
      yield response

    case request @ PATCH -> Root / "api" / "messages" / messageIdValue =>
      for
        currentUserId <- requireCurrentUserId(request)
        editRequest <- request.as[EditTourGroupMessageRequestDto]
        currentTime <- currentInstantF
        messages <- tourGroupApplicationService.editMessage(TourGroupMessageId(messageIdValue), currentUserId, editRequest.content, currentTime)
        response <- Ok(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)).asJson)
      yield response

    case request @ POST -> Root / "api" / "messages" / messageIdValue / "delete" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        messages <- tourGroupApplicationService.deleteMessage(TourGroupMessageId(messageIdValue), currentUserId, currentTime)
        response <- Ok(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)).asJson)
      yield response

    case request @ POST -> Root / "api" / "messages" / messageIdValue / "recall" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        messages <- tourGroupApplicationService.recallMessage(TourGroupMessageId(messageIdValue), currentUserId, currentTime)
        response <- Ok(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)).asJson)
      yield response

    case request @ POST -> Root / "api" / "messages" / messageIdValue / "reactions" =>
      for
        currentUserId <- requireCurrentUserId(request)
        reactionRequest <- request.as[ReactTourGroupMessageRequestDto]
        currentTime <- currentInstantF
        messages <- tourGroupApplicationService.addReaction(TourGroupMessageId(messageIdValue), currentUserId, reactionRequest.reactionType, currentTime)
        response <- Ok(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)).asJson)
      yield response

    case request @ DELETE -> Root / "api" / "messages" / messageIdValue / "reactions" / reactionTypeValue =>
      for
        currentUserId <- requireCurrentUserId(request)
        messages <- tourGroupApplicationService.removeReaction(TourGroupMessageId(messageIdValue), currentUserId, reactionTypeValue)
        response <- Ok(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)).asJson)
      yield response
  }
