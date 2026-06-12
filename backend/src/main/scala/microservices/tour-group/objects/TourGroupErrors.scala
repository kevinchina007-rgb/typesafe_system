// TourGroupErrors 定义团体游模块的错误模型。

package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*

import java.time.LocalDate

sealed trait TourGroupError extends DomainError

object TourGroupError:
  final case class GroupCapacityWasInvalid(capacity: Int) extends TourGroupError:
    override val message: String =
      s"Tour group capacity '$capacity' must be greater than zero"

  final case class GroupDateRangeWasInvalid(startDate: LocalDate, endDate: LocalDate) extends TourGroupError:
    override val message: String =
      s"Tour group start date '$startDate' must be before end date '$endDate'"

  final case class GroupWasNotFound(groupId: TourGroupId) extends TourGroupError:
    override val message: String =
      s"Tour group '${groupId.value}' was not found"

  final case class MembershipWasNotFound(membershipId: TourGroupMembershipId) extends TourGroupError:
    override val message: String =
      s"Tour group membership '${membershipId.value}' was not found"

  final case class ActiveMembershipAlreadyExists(groupId: TourGroupId, userId: UserId) extends TourGroupError:
    override val message: String =
      s"User '${userId.value}' already has an active membership in group '${groupId.value}'"

  final case class UserWasBlacklistedFromGroup(groupId: TourGroupId, userId: UserId) extends TourGroupError:
    override val message: String =
      s"User '${userId.value}' was blacklisted from group '${groupId.value}'"

  final case class PlanItemWasNotFound(planItemId: GroupPlanItemId) extends TourGroupError:
    override val message: String =
      s"Group plan item '${planItemId.value}' was not found"

  final case class PlanOptionWasNotFound(optionId: GroupPlanOptionId) extends TourGroupError:
    override val message: String =
      s"Group plan option '${optionId.value}' was not found"

  final case class SelectionWasNotFound(selectionId: GroupPlanSelectionId) extends TourGroupError:
    override val message: String =
      s"Group selection '${selectionId.value}' was not found"

  final case class OrganizerScopeDidNotMatch(groupId: TourGroupId, actingUserId: UserId) extends TourGroupError:
    override val message: String =
      s"User '${actingUserId.value}' is not organizer of group '${groupId.value}'"

  final case class OrganizerMustTransferBeforeLeaving(groupId: TourGroupId, actingUserId: UserId) extends TourGroupError:
    override val message: String =
      s"User '${actingUserId.value}' must transfer organizer role before leaving group '${groupId.value}'"

  final case class OrganizerCannotBeKicked(groupId: TourGroupId, targetUserId: UserId) extends TourGroupError:
    override val message: String =
      s"Organizer '${targetUserId.value}' cannot be kicked from group '${groupId.value}'"

  final case class OrganizerCannotBeBlacklisted(groupId: TourGroupId, targetUserId: UserId) extends TourGroupError:
    override val message: String =
      s"Organizer '${targetUserId.value}' cannot be blacklisted from group '${groupId.value}'"

  final case class OrganizerTransferTargetWasInvalid(groupId: TourGroupId, targetUserId: UserId) extends TourGroupError:
    override val message: String =
      s"User '${targetUserId.value}' cannot become organizer of group '${groupId.value}'"

  final case class MembershipScopeDidNotMatch(membershipId: TourGroupMembershipId, actingUserId: UserId)
      extends TourGroupError:
    override val message: String =
      s"User '${actingUserId.value}' does not own membership '${membershipId.value}'"

  final case class MembershipTravelerDidNotBelongToUser(travelerId: TravelerId, actingUserId: UserId)
      extends TourGroupError:
    override val message: String =
      s"Traveler '${travelerId.value}' does not belong to user '${actingUserId.value}'"

  final case class MembershipTravelerAlreadyExists(membershipId: TourGroupMembershipId, travelerId: TravelerId)
      extends TourGroupError:
    override val message: String =
      s"Traveler '${travelerId.value}' is already part of membership '${membershipId.value}'"

  final case class GroupCapacityWasExceeded(groupId: TourGroupId, capacity: Int, requestedUsedCapacity: Int)
      extends TourGroupError:
    override val message: String =
      s"Tour group '${groupId.value}' capacity $capacity would be exceeded by used capacity $requestedUsedCapacity"

  final case class GroupWasNotOpen(groupId: TourGroupId, status: TourGroupStatus) extends TourGroupError:
    override val message: String =
      s"Tour group '${groupId.value}' is not open in status $status"

  final case class PlanOptionDidNotBelongToPlanItem(optionId: GroupPlanOptionId, planItemId: GroupPlanItemId)
      extends TourGroupError:
    override val message: String =
      s"Plan option '${optionId.value}' does not belong to plan item '${planItemId.value}'"

  final case class PlanOptionResourceTypeDidNotMatchPlanItem(
      planItemId: GroupPlanItemId,
      itemType: GroupPlanItemType,
      resourceType: GroupPlanOptionResourceType
  ) extends TourGroupError:
    override val message: String =
      s"Plan item '${planItemId.value}' of type $itemType cannot use resource type $resourceType"

  final case class SelectionTravelerWasEmpty(planItemId: GroupPlanItemId) extends TourGroupError:
    override val message: String =
      s"Plan item '${planItemId.value}' requires at least one traveler"

  final case class SelectionTravelerWasNotInMembership(selectionId: GroupPlanSelectionId, travelerId: TravelerId)
      extends TourGroupError:
    override val message: String =
      s"Traveler '${travelerId.value}' is not part of selection '${selectionId.value}' membership"

  final case class SelectionQuantityWasInvalid(quantity: Int) extends TourGroupError:
    override val message: String =
      s"Selection quantity '$quantity' must be greater than zero"

  final case class SelectionWasNotSubmittable(selectionId: GroupPlanSelectionId, status: GroupPlanSelectionStatus)
      extends TourGroupError:
    override val message: String =
      s"Selection '${selectionId.value}' cannot be submitted from status $status"

  final case class SelectionWasNotConfirmable(selectionId: GroupPlanSelectionId, status: GroupPlanSelectionStatus)
      extends TourGroupError:
    override val message: String =
      s"Selection '${selectionId.value}' cannot be confirmed from status $status"

  final case class SelectionWasNotRejectable(selectionId: GroupPlanSelectionId, status: GroupPlanSelectionStatus)
      extends TourGroupError:
    override val message: String =
      s"Selection '${selectionId.value}' cannot be rejected from status $status"

  final case class SelectionWasNotPayable(selectionId: GroupPlanSelectionId, status: GroupPlanSelectionStatus)
      extends TourGroupError:
    override val message: String =
      s"Selection '${selectionId.value}' cannot be paid from status $status"

  final case class SelectionReviewNoteWasEmpty(selectionId: GroupPlanSelectionId) extends TourGroupError:
    override val message: String =
      s"Selection '${selectionId.value}' reject review note must not be empty"

  final case class SelectionQuantityDidNotMatchTravelerCount(
      selectionId: GroupPlanSelectionId,
      quantity: Int,
      travelerCount: Int
  ) extends TourGroupError:
    override val message: String =
      s"Selection '${selectionId.value}' quantity $quantity must match traveler count $travelerCount"

  final case class PlanItemSequenceWasInvalid(sequenceNo: Int) extends TourGroupError:
    override val message: String =
      s"Group plan item sequence '$sequenceNo' must be greater than zero"

  final case class PlanItemTimeWindowWasInvalid(planItemId: GroupPlanItemId) extends TourGroupError:
    override val message: String =
      s"Plan item '${planItemId.value}' has an invalid time window"

  final case class SelectionWasAlreadyLinked(selectionId: GroupPlanSelectionId, orderId: OrderId) extends TourGroupError:
    override val message: String =
      s"Selection '${selectionId.value}' is already linked to order '${orderId.value}'"

  final case class RequiredFieldWasEmpty(fieldName: String) extends TourGroupError:
    override val message: String =
      s"Field '$fieldName' must not be empty"

  final case class ConversationWasNotFound(conversationId: TourGroupConversationId) extends TourGroupError:
    override val message: String =
      s"Conversation '${conversationId.value}' was not found"

  final case class GroupMemberWasNotFound(groupId: TourGroupId, userId: UserId) extends TourGroupError:
    override val message: String =
      s"User '${userId.value}' is not an active member of group '${groupId.value}'"

  final case class DirectConversationWasNotAllowed(groupId: TourGroupId, actingUserId: UserId, targetUserId: UserId)
      extends TourGroupError:
    override val message: String =
      s"User '${actingUserId.value}' cannot start a direct conversation with '${targetUserId.value}' in group '${groupId.value}'"

  final case class ConversationAccessWasDenied(conversationId: TourGroupConversationId, actingUserId: UserId)
      extends TourGroupError:
    override val message: String =
      s"User '${actingUserId.value}' cannot access conversation '${conversationId.value}'"

  final case class MessageContentWasEmpty() extends TourGroupError:
    override val message: String =
      "Message content must not be empty"

  final case class DirectConversationTargetWasInvalid(userId: UserId) extends TourGroupError:
    override val message: String =
      s"User '${userId.value}' cannot start a direct conversation with themselves"

  final case class ConversationWasClosed(conversationId: TourGroupConversationId) extends TourGroupError:
    override val message: String =
      s"Conversation '${conversationId.value}' is closed"

  final case class MessageWasNotFound(messageId: TourGroupMessageId) extends TourGroupError:
    override val message: String =
      s"Message '${messageId.value}' was not found"

  final case class MessageAccessWasDenied(messageId: TourGroupMessageId, actingUserId: UserId) extends TourGroupError:
    override val message: String =
      s"User '${actingUserId.value}' cannot modify message '${messageId.value}'"

  final case class MessageEditWasNotAllowed(messageId: TourGroupMessageId, status: TourGroupMessageStatus)
      extends TourGroupError:
    override val message: String =
      s"Message '${messageId.value}' cannot be edited from status $status"

  final case class MessageDeleteWasNotAllowed(messageId: TourGroupMessageId, status: TourGroupMessageStatus)
      extends TourGroupError:
    override val message: String =
      s"Message '${messageId.value}' cannot be deleted from status $status"

  final case class MessageRecallWasNotAllowed(messageId: TourGroupMessageId, status: TourGroupMessageStatus)
      extends TourGroupError:
    override val message: String =
      s"Message '${messageId.value}' cannot be recalled from status $status"

  final case class MessageReactionTypeWasInvalid(reactionType: String) extends TourGroupError:
    override val message: String =
      s"Reaction '$reactionType' is not supported"

  final case class MessageAttachmentWasNotFound(attachmentId: TourGroupMessageAttachmentId) extends TourGroupError:
    override val message: String =
      s"Attachment '${attachmentId.value}' was not found"

  final case class MessageAttachmentUploadWasNotAllowed(reason: String) extends TourGroupError:
    override val message: String =
      s"Message attachment upload was not allowed: $reason"
