package com.typesafe.travel.tourgroup.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*

import java.time.{Instant, LocalDate}

// TourGroup 只负责“团体计划与成员关系”，不直接承担真实交易。
// 真实支付和退款仍然通过 Order 主链完成。
enum TourGroupStatus:
  case Draft, Open, Closed, Cancelled

final case class TourGroup(
    groupId: TourGroupId,
    organizerUserId: UserId,
    title: String,
    description: String,
    destination: String,
    startDate: LocalDate,
    endDate: LocalDate,
    capacity: Int,
    status: TourGroupStatus,
    createdAt: Instant
):
  def ensureOrganizer(actingUserId: UserId): Either[TourGroupError, TourGroup] =
    Either.cond(organizerUserId == actingUserId, this, TourGroupError.OrganizerScopeDidNotMatch(groupId, actingUserId))

  def ensureOpen: Either[TourGroupError, TourGroup] =
    Either.cond(status == TourGroupStatus.Open, this, TourGroupError.GroupWasNotOpen(groupId, status))

final case class TourGroupDetails(
    group: TourGroup,
    memberships: Vector[TourGroupMembership],
    membershipTravelers: Vector[TourGroupMembershipTraveler],
    planItems: Vector[GroupPlanItem],
    planOptions: Vector[GroupPlanOption],
    selections: Vector[GroupPlanSelection],
    selectionTravelers: Vector[GroupPlanSelectionTraveler],
    selectionOrderLinks: Vector[GroupSelectionOrderLink]
):
  // usedCapacity / isFull 都是派生信息，不落成新的权威字段。
  lazy val usedCapacity: Int =
    membershipTravelers.count(_.status == TourGroupMembershipTravelerStatus.Active)

  lazy val isFull: Boolean =
    usedCapacity >= group.capacity

  def membershipById(membershipId: TourGroupMembershipId): Either[TourGroupError, TourGroupMembership] =
    memberships.find(_.membershipId == membershipId).toRight(TourGroupError.MembershipWasNotFound(membershipId))

  def planItemById(planItemId: GroupPlanItemId): Either[TourGroupError, GroupPlanItem] =
    planItems.find(_.planItemId == planItemId).toRight(TourGroupError.PlanItemWasNotFound(planItemId))

  def planOptionById(optionId: GroupPlanOptionId): Either[TourGroupError, GroupPlanOption] =
    planOptions.find(_.optionId == optionId).toRight(TourGroupError.PlanOptionWasNotFound(optionId))

  def selectionById(selectionId: GroupPlanSelectionId): Either[TourGroupError, GroupPlanSelection] =
    selections.find(_.selectionId == selectionId).toRight(TourGroupError.SelectionWasNotFound(selectionId))

  def membershipTravelersFor(membershipId: TourGroupMembershipId): Vector[TourGroupMembershipTraveler] =
    membershipTravelers.filter(_.membershipId == membershipId)

  def selectionTravelersFor(selectionId: GroupPlanSelectionId): Vector[GroupPlanSelectionTraveler] =
    selectionTravelers.filter(_.selectionId == selectionId)

