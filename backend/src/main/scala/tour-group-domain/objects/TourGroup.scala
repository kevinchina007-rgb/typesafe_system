package com.typesafe.travel.tourgroup.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*

import java.time.{Instant, LocalDate}

// TourGroup 只负责“团体计划与成员关系”，不直接承担真实交易。
// 真实支付和退款仍然通过 Order 主链完成。
enum TourGroupStatus:
  case Draft, Open, Closed, Cancelled

object TourGroupStatus:
  val all: Vector[TourGroupStatus] =
    Vector(TourGroupStatus.Draft, TourGroupStatus.Open, TourGroupStatus.Closed, TourGroupStatus.Cancelled)

  def fromText(value: String): TourGroupStatus =
    value.trim match
      case "Draft"     => TourGroupStatus.Draft
      case "Open"      => TourGroupStatus.Open
      case "Closed"    => TourGroupStatus.Closed
      case "Cancelled" => TourGroupStatus.Cancelled
      case other       => throw new IllegalArgumentException(s"Unknown tour group status: $other")

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

def createTourGroup(
    groupId: TourGroupId,
    organizerUserId: UserId,
    title: String,
    description: String,
    destination: String,
    startDate: LocalDate,
    endDate: LocalDate,
    capacity: Int,
    createdAt: Instant
): Either[TourGroupError, TourGroup] =
  for
    _ <- Either.cond(capacity > 0, (), TourGroupError.GroupCapacityWasInvalid(capacity))
    _ <- Either.cond(startDate.isBefore(endDate), (), TourGroupError.GroupDateRangeWasInvalid(startDate, endDate))
    normalizedTitle <- normalizeRequiredTourGroupText("tour-group-title", title)
    normalizedDescription <- normalizeRequiredTourGroupText("tour-group-description", description)
    normalizedDestination <- normalizeRequiredTourGroupText("tour-group-destination", destination)
  yield TourGroup(
    groupId,
    organizerUserId,
    normalizedTitle,
    normalizedDescription,
    normalizedDestination,
    startDate,
    endDate,
    capacity,
    TourGroupStatus.Open,
    createdAt
  )

def createOrganizerMembership(
    membershipId: TourGroupMembershipId,
    groupId: TourGroupId,
    organizerUserId: UserId,
    joinedAt: Instant
): TourGroupMembership =
  TourGroupMembership(membershipId, groupId, organizerUserId, joinedAt, TourGroupMembershipStatus.Active)

def createMemberMembership(
    membershipId: TourGroupMembershipId,
    groupId: TourGroupId,
    userId: UserId,
    joinedAt: Instant
): TourGroupMembership =
  TourGroupMembership(membershipId, groupId, userId, joinedAt, TourGroupMembershipStatus.Active)

private def normalizeRequiredTourGroupText(fieldName: String, value: String): Either[TourGroupError, String] =
  val normalized = value.trim
  Either.cond(normalized.nonEmpty, normalized, TourGroupError.RequiredFieldWasEmpty(fieldName))

