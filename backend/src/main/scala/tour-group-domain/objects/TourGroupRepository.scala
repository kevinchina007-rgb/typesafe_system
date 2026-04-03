package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*

trait TourGroupRepository[F[_]]:
  def nextGroupId: F[TourGroupId]
  def nextMembershipId: F[TourGroupMembershipId]
  def nextMembershipTravelerId: F[TourGroupMembershipTravelerId]
  def nextPlanItemId: F[GroupPlanItemId]
  def nextPlanOptionId: F[GroupPlanOptionId]
  def nextSelectionId: F[GroupPlanSelectionId]
  def nextSelectionTravelerId: F[GroupPlanSelectionTravelerId]
  def nextSelectionOrderLinkId: F[GroupSelectionOrderLinkId]
  def nextConversationId: F[TourGroupConversationId]
  def nextConversationParticipantId: F[TourGroupConversationParticipantId]
  def nextMessageId: F[TourGroupMessageId]
  def nextMessageAttachmentId: F[TourGroupMessageAttachmentId]
  def nextMessageReactionId: F[TourGroupMessageReactionId]

  def saveGroup(group: TourGroup): F[TourGroup]
  def findGroupById(groupId: TourGroupId): F[Option[TourGroup]]
  def listGroups: F[List[TourGroup]]

  def saveMembership(membership: TourGroupMembership): F[TourGroupMembership]
  def findMembershipById(membershipId: TourGroupMembershipId): F[Option[TourGroupMembership]]
  def findMembershipsByGroupId(groupId: TourGroupId): F[List[TourGroupMembership]]
  def findActiveMembershipByGroupIdAndUserId(groupId: TourGroupId, userId: UserId): F[Option[TourGroupMembership]]

  def saveMembershipTraveler(membershipTraveler: TourGroupMembershipTraveler): F[TourGroupMembershipTraveler]
  def findMembershipTravelersByMembershipId(membershipId: TourGroupMembershipId): F[List[TourGroupMembershipTraveler]]
  def findMembershipTravelersByGroupId(groupId: TourGroupId): F[List[TourGroupMembershipTraveler]]

  def savePlanItem(planItem: GroupPlanItem): F[GroupPlanItem]
  def findPlanItemById(planItemId: GroupPlanItemId): F[Option[GroupPlanItem]]
  def findPlanItemsByGroupId(groupId: TourGroupId): F[List[GroupPlanItem]]

  def savePlanOption(planOption: GroupPlanOption): F[GroupPlanOption]
  def findPlanOptionById(optionId: GroupPlanOptionId): F[Option[GroupPlanOption]]
  def findPlanOptionsByPlanItemId(planItemId: GroupPlanItemId): F[List[GroupPlanOption]]
  def findPlanOptionsByGroupId(groupId: TourGroupId): F[List[GroupPlanOption]]

  def saveSelection(selection: GroupPlanSelection): F[GroupPlanSelection]
  def findSelectionById(selectionId: GroupPlanSelectionId): F[Option[GroupPlanSelection]]
  def findSelectionsByGroupId(groupId: TourGroupId): F[List[GroupPlanSelection]]

  def saveSelectionTraveler(selectionTraveler: GroupPlanSelectionTraveler): F[GroupPlanSelectionTraveler]
  def findSelectionTravelersBySelectionId(selectionId: GroupPlanSelectionId): F[List[GroupPlanSelectionTraveler]]
  def findSelectionTravelersByGroupId(groupId: TourGroupId): F[List[GroupPlanSelectionTraveler]]

  def saveSelectionOrderLink(link: GroupSelectionOrderLink): F[GroupSelectionOrderLink]
  def findSelectionOrderLinkBySelectionId(selectionId: GroupPlanSelectionId): F[Option[GroupSelectionOrderLink]]
  def findSelectionOrderLinksByGroupId(groupId: TourGroupId): F[List[GroupSelectionOrderLink]]

  def saveChatSettings(settings: TourGroupChatSettings): F[TourGroupChatSettings]
  def findChatSettingsByGroupId(groupId: TourGroupId): F[Option[TourGroupChatSettings]]

  def saveConversation(conversation: TourGroupConversation): F[TourGroupConversation]
  def findConversationById(conversationId: TourGroupConversationId): F[Option[TourGroupConversation]]
  def findPublicConversationByGroupId(groupId: TourGroupId): F[Option[TourGroupConversation]]
  def findDirectConversationByGroupIdAndUsers(groupId: TourGroupId, userA: UserId, userB: UserId): F[Option[TourGroupConversation]]
  def findDirectConversationsByGroupIdAndUserId(groupId: TourGroupId, userId: UserId): F[List[TourGroupConversation]]
  def findAccessibleConversationsByGroupIdAndUserId(groupId: TourGroupId, userId: UserId): F[List[TourGroupConversation]]

  def saveConversationParticipant(participant: TourGroupConversationParticipant): F[TourGroupConversationParticipant]
  def findConversationParticipant(conversationId: TourGroupConversationId, userId: UserId): F[Option[TourGroupConversationParticipant]]
  def findParticipantsByConversationId(conversationId: TourGroupConversationId): F[List[TourGroupConversationParticipant]]

  def saveMessage(message: TourGroupMessage): F[TourGroupMessage]
  def findMessageById(messageId: TourGroupMessageId): F[Option[TourGroupMessage]]
  def findMessagesByConversationId(conversationId: TourGroupConversationId): F[List[TourGroupMessage]]
  def saveMessageAttachment(attachment: TourGroupMessageAttachment): F[TourGroupMessageAttachment]
  def findAttachmentsByMessageId(messageId: TourGroupMessageId): F[List[TourGroupMessageAttachment]]
  def findAttachmentsByMessageIds(messageIds: List[TourGroupMessageId]): F[Map[TourGroupMessageId, List[TourGroupMessageAttachment]]]
  def saveMessageReaction(reaction: TourGroupMessageReaction): F[TourGroupMessageReaction]
  def deleteMessageReaction(messageId: TourGroupMessageId, userId: UserId, reactionType: String): F[Unit]
  def findReactionsByMessageIds(messageIds: List[TourGroupMessageId]): F[Map[TourGroupMessageId, List[TourGroupMessageReaction]]]
  def searchMessagesByGroupIdAndUserId(groupId: TourGroupId, userId: UserId, query: String): F[List[TourGroupMessage]]

  def findGroupDetails(groupId: TourGroupId): F[Option[TourGroupDetails]]
