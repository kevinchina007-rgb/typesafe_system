package com.typesafe.travel.api.routes

import cats.MonadThrow
import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.tourgroup.domain.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
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
        response <- createdJson(TourGroupDetailsResponseDto.fromView(detailsView))
      yield response

    case GET -> Root / "api" / "tour-groups" =>
      tourGroupApplicationService.listGroups.flatMap(groups => okJson(TourGroupListResponseDto(groups.map(TourGroupSummaryResponseDto.fromView))))

    case GET -> Root / "api" / "tour-groups" / groupIdValue =>
      tourGroupApplicationService.getGroupDetails(TourGroupId(groupIdValue)).flatMap(view => okJson(TourGroupDetailsResponseDto.fromView(view)))

    case request @ POST -> Root / "api" / "tour-groups" / groupIdValue / "memberships" =>
      for
        currentUserId <- requireCurrentUserId(request)
        joinRequest <- request.as[JoinTourGroupRequestDto]
        joinedAt <- currentInstantF
        detailsView <- tourGroupApplicationService.joinGroup(TourGroupId(groupIdValue), currentUserId, joinedAt)
        response <- okJson(TourGroupDetailsResponseDto.fromView(detailsView))
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
        response <- okJson(TourGroupDetailsResponseDto.fromView(detailsView))
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
        response <- okJson(TourGroupDetailsResponseDto.fromView(detailsView))
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
        response <- okJson(TourGroupDetailsResponseDto.fromView(detailsView))
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
        response <- okJson(TourGroupDetailsResponseDto.fromView(detailsView))
      yield response

    case request @ POST -> Root / "api" / "selections" / selectionIdValue / "submit" =>
      for
        currentUserId <- requireCurrentUserId(request)
        submitRequest <- request.as[SubmitGroupPlanSelectionRequestDto]
        detailsView <- tourGroupApplicationService.submitSelection(GroupPlanSelectionId(selectionIdValue), currentUserId)
        response <- okJson(TourGroupDetailsResponseDto.fromView(detailsView))
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
        response <- okJson(TourGroupDetailsResponseDto.fromView(detailsView))
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
        response <- okJson(TourGroupDetailsResponseDto.fromView(detailsView))
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
        response <- okJson(TourGroupBatchPayResponseDto(TourGroupDetailsResponseDto.fromView(detailsView), List(orderResponseDto)))
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
        response <- okJson(TourGroupBatchPayResponseDto(TourGroupDetailsResponseDto.fromView(detailsView), orderResponseDtos))
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
        response <- okJson(TourGroupDetailsResponseDto.fromView(detailsView))
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
        response <- okJson(TourGroupDetailsResponseDto.fromView(detailsView))
      yield response

    case GET -> Root / "api" / "tour-groups" / groupIdValue / "bookings" =>
      tourGroupApplicationService.listGroupBookings(TourGroupId(groupIdValue)).flatMap { orders =>
        orders.traverse(toOrderResponseDto).flatMap(orderDtos => okJson(OrderListResponseDto(orderDtos)))
      }

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "chat-settings" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        settingsView <- tourGroupApplicationService.getChatSettings(TourGroupId(groupIdValue), currentUserId, currentTime)
        response <- okJson(TourGroupChatSettingsResponseDto.fromView(settingsView))
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
        response <- okJson(TourGroupChatSettingsResponseDto.fromView(settingsView))
      yield response

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "conversations" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        conversations <- tourGroupApplicationService.listConversations(TourGroupId(groupIdValue), currentUserId, currentTime)
        response <- okJson(TourGroupConversationListResponseDto.fromView(conversations))
      yield response

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "chat" / "conversation" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        conversations <- tourGroupApplicationService.listConversations(TourGroupId(groupIdValue), currentUserId, currentTime)
        response <- okJson(TourGroupConversationListResponseDto.fromView(conversations))
      yield response

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "chat" / "messages" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        messageViews <- tourGroupApplicationService.listGroupChatMessages(TourGroupId(groupIdValue), currentUserId, currentTime)
        response <- okJson(TourGroupMessageListResponseDto(messageViews.map(TourGroupMessageResponseDto.fromView)))
      yield response

    case request @ POST -> Root / "api" / "tour-groups" / groupIdValue / "chat" / "messages" =>
      for
        currentUserId <- requireCurrentUserId(request)
        messageRequest <- request.as[SendTourGroupMessageRequestDto]
        createdAt <- currentInstantF
        messageViews <- tourGroupApplicationService.sendGroupChatMessage(TourGroupId(groupIdValue), currentUserId, messageRequest.content, createdAt)
        response <- okJson(TourGroupMessageListResponseDto(messageViews.map(TourGroupMessageResponseDto.fromView)))
      yield response

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "chat" / "search" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        query <- fromEither(request.params.get("q").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("q")))
        results <- tourGroupApplicationService.searchMessages(TourGroupId(groupIdValue), currentUserId, query, currentTime)
        response <- okJson(TourGroupMessageSearchResponseDto(results.map(TourGroupMessageSearchResultResponseDto.fromView)))
      yield response

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "chat" / "conversations" / "search" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        query <- fromEither(request.params.get("q").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("q")))
        results <- tourGroupApplicationService.searchConversations(TourGroupId(groupIdValue), currentUserId, query, currentTime)
        response <- okJson(TourGroupConversationListResponseDto(results.map(TourGroupConversationSummaryResponseDto.fromView), None))
      yield response

    case request @ GET -> Root / "api" / "tour-groups" / groupIdValue / "direct-conversations" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        conversations <- tourGroupApplicationService.listDirectConversations(TourGroupId(groupIdValue), currentUserId, currentTime)
        response <- okJson(TourGroupConversationListResponseDto(conversations.map(TourGroupConversationSummaryResponseDto.fromView), None))
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
        response <- okJson(TourGroupConversationSummaryResponseDto.fromView(conversation))
      yield response

    case request @ POST -> Root / "api" / "conversations" / conversationIdValue / "attachments" =>
      for
        currentUserId <- requireCurrentUserId(request)
        multipartPayload <- request.as[Multipart[F]]
        filePart <- requireMultipartPart(multipartPayload, "attachment", SharedValidationError.RequiredFieldWasEmpty("attachment"))
        uploadedAttachment <- readUploadedBinary(filePart, SharedValidationError.RequiredFieldWasEmpty("attachment.filename"))
        groupIdValue <- requireQueryParam(request, "groupId")
        currentTime <- currentInstantF
        uploaded <- tourGroupApplicationService.uploadConversationAttachment(
          TourGroupId(groupIdValue),
          currentUserId,
          uploadedAttachment.originalFileName,
          uploadedAttachment.contentTypeValue,
          uploadedAttachment.fileBytes,
          currentTime
        )
        response <- createdJson(TourGroupUploadedAttachmentResponseDto.fromView(uploaded))
      yield response

    case request @ GET -> Root / "api" / "conversations" / conversationIdValue / "messages" =>
      for
        currentUserId <- requireCurrentUserId(request)
        messages <- tourGroupApplicationService.getConversationMessages(TourGroupConversationId(conversationIdValue), currentUserId)
        response <- okJson(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)))
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
        response <- okJson(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)))
      yield response

    case request @ POST -> Root / "api" / "conversations" / conversationIdValue / "read" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        summary <- tourGroupApplicationService.markConversationRead(TourGroupConversationId(conversationIdValue), currentUserId, currentTime)
        response <- okJson(TourGroupConversationSummaryResponseDto.fromView(summary))
      yield response

    case request @ GET -> Root / "api" / "direct-conversations" / conversationIdValue / "messages" =>
      for
        currentUserId <- requireCurrentUserId(request)
        messageViews <- tourGroupApplicationService.listDirectConversationMessages(TourGroupConversationId(conversationIdValue), currentUserId)
        response <- okJson(TourGroupMessageListResponseDto(messageViews.map(TourGroupMessageResponseDto.fromView)))
      yield response

    case request @ PATCH -> Root / "api" / "direct-conversations" / conversationIdValue / "mute" =>
      for
        currentUserId <- requireCurrentUserId(request)
        muteRequest <- request.as[UpdateConversationMuteRequestDto]
        currentTime <- currentInstantF
        summary <- tourGroupApplicationService.updateConversationMuteState(TourGroupConversationId(conversationIdValue), currentUserId, muteRequest.muted, currentTime)
        response <- okJson(TourGroupConversationSummaryResponseDto.fromView(summary))
      yield response

    case request @ PATCH -> Root / "api" / "direct-conversations" / conversationIdValue / "archive" =>
      for
        currentUserId <- requireCurrentUserId(request)
        archiveRequest <- request.as[UpdateConversationArchiveRequestDto]
        currentTime <- currentInstantF
        summary <- tourGroupApplicationService.updateConversationArchiveState(TourGroupConversationId(conversationIdValue), currentUserId, archiveRequest.archived, currentTime)
        response <- okJson(TourGroupConversationSummaryResponseDto.fromView(summary))
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
        response <- okJson(TourGroupMessageListResponseDto(messageViews.map(TourGroupMessageResponseDto.fromView)))
      yield response

    case request @ PATCH -> Root / "api" / "messages" / messageIdValue =>
      for
        currentUserId <- requireCurrentUserId(request)
        editRequest <- request.as[EditTourGroupMessageRequestDto]
        currentTime <- currentInstantF
        messages <- tourGroupApplicationService.editMessage(TourGroupMessageId(messageIdValue), currentUserId, editRequest.content, currentTime)
        response <- okJson(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)))
      yield response

    case request @ POST -> Root / "api" / "messages" / messageIdValue / "delete" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        messages <- tourGroupApplicationService.deleteMessage(TourGroupMessageId(messageIdValue), currentUserId, currentTime)
        response <- okJson(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)))
      yield response

    case request @ POST -> Root / "api" / "messages" / messageIdValue / "recall" =>
      for
        currentUserId <- requireCurrentUserId(request)
        currentTime <- currentInstantF
        messages <- tourGroupApplicationService.recallMessage(TourGroupMessageId(messageIdValue), currentUserId, currentTime)
        response <- okJson(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)))
      yield response

    case request @ POST -> Root / "api" / "messages" / messageIdValue / "reactions" =>
      for
        currentUserId <- requireCurrentUserId(request)
        reactionRequest <- request.as[ReactTourGroupMessageRequestDto]
        currentTime <- currentInstantF
        messages <- tourGroupApplicationService.addReaction(TourGroupMessageId(messageIdValue), currentUserId, reactionRequest.reactionType, currentTime)
        response <- okJson(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)))
      yield response

    case request @ DELETE -> Root / "api" / "messages" / messageIdValue / "reactions" / reactionTypeValue =>
      for
        currentUserId <- requireCurrentUserId(request)
        messages <- tourGroupApplicationService.removeReaction(TourGroupMessageId(messageIdValue), currentUserId, reactionTypeValue)
        response <- okJson(TourGroupMessageListResponseDto(messages.map(TourGroupMessageResponseDto.fromView)))
      yield response
  }
