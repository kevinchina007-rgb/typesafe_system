// TourGroupIdentifiers 定义共享内核中的共享内核中的通用数据模型。

package com.typesafe.travel.shared.kernel

final case class TourGroupId(value: String) extends AnyVal
final case class TourGroupMembershipId(value: String) extends AnyVal
final case class TourGroupMembershipTravelerId(value: String) extends AnyVal
final case class GroupPlanItemId(value: String) extends AnyVal
final case class GroupPlanOptionId(value: String) extends AnyVal
final case class GroupPlanSelectionId(value: String) extends AnyVal
final case class GroupPlanSelectionTravelerId(value: String) extends AnyVal
final case class GroupSelectionOrderLinkId(value: String) extends AnyVal
final case class TourGroupConversationId(value: String) extends AnyVal
final case class TourGroupConversationParticipantId(value: String) extends AnyVal
final case class TourGroupMessageId(value: String) extends AnyVal
final case class TourGroupMessageAttachmentId(value: String) extends AnyVal
final case class TourGroupMessageReactionId(value: String) extends AnyVal

