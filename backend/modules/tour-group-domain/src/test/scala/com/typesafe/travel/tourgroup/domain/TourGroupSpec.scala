package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.{Instant, LocalDate}

final class TourGroupSpec extends FunSuite:
  private val organizerUserId = UserId("user-organizer")
  private val createdAt = Instant.parse("2026-03-30T08:00:00Z")

  test("group creation validates capacity and date range") {
    val invalidCapacity =
      TourGroup.create(
        groupId = TourGroupId("group-1"),
        organizerUserId = organizerUserId,
        title = "Spring Tour",
        description = "Trip",
        destination = "Suzhou",
        startDate = LocalDate.parse("2026-05-01"),
        endDate = LocalDate.parse("2026-05-03"),
        capacity = 0,
        createdAt = createdAt
      )

    val invalidDates =
      TourGroup.create(
        groupId = TourGroupId("group-1"),
        organizerUserId = organizerUserId,
        title = "Spring Tour",
        description = "Trip",
        destination = "Suzhou",
        startDate = LocalDate.parse("2026-05-03"),
        endDate = LocalDate.parse("2026-05-03"),
        capacity = 2,
        createdAt = createdAt
      )

    assert(invalidCapacity.isLeft)
    assert(invalidDates.isLeft)
  }

  test("used capacity is derived from active membership travelers") {
    val group = TourGroup.create(
      groupId = TourGroupId("group-1"),
      organizerUserId = organizerUserId,
      title = "Spring Tour",
      description = "Trip",
      destination = "Suzhou",
      startDate = LocalDate.parse("2026-05-01"),
      endDate = LocalDate.parse("2026-05-03"),
      capacity = 4,
      createdAt = createdAt
    ).toOption.get

    val membership = TourGroupMembership.createMemberMembership(
      membershipId = TourGroupMembershipId("membership-1"),
      groupId = group.groupId,
      userId = UserId("user-member"),
      joinedAt = createdAt
    )

    val details = TourGroupDetails(
      group = group,
      memberships = Vector(membership),
      membershipTravelers = Vector(
        TourGroupMembershipTraveler(TourGroupMembershipTravelerId("mt-1"), membership.membershipId, TravelerId("traveler-1"), createdAt, TourGroupMembershipTravelerStatus.Active),
        TourGroupMembershipTraveler(TourGroupMembershipTravelerId("mt-2"), membership.membershipId, TravelerId("traveler-2"), createdAt, TourGroupMembershipTravelerStatus.Active),
        TourGroupMembershipTraveler(TourGroupMembershipTravelerId("mt-3"), membership.membershipId, TravelerId("traveler-3"), createdAt, TourGroupMembershipTravelerStatus.Removed)
      ),
      planItems = Vector.empty,
      planOptions = Vector.empty,
      selections = Vector.empty,
      selectionTravelers = Vector.empty,
      selectionOrderLinks = Vector.empty
    )

    assertEquals(details.usedCapacity, 2)
    assertEquals(details.isFull, false)
  }

  test("selection transitions from draft to submitted to confirmed") {
    val selection = GroupPlanSelection.create(
      selectionId = GroupPlanSelectionId("selection-1"),
      groupId = TourGroupId("group-1"),
      planItemId = GroupPlanItemId("plan-item-1"),
      optionId = GroupPlanOptionId("option-1"),
      membershipId = TourGroupMembershipId("membership-1"),
      quantity = 2,
      createdAt = createdAt
    ).toOption.get

    val submitted = selection.submit.toOption.get
    val confirmed = submitted.confirm(UserId("organizer-1"), createdAt.plusSeconds(30), Some("Looks good")).toOption.get

    assertEquals(submitted.status, GroupPlanSelectionStatus.Submitted)
    assertEquals(confirmed.status, GroupPlanSelectionStatus.OrganizerConfirmed)
    assertEquals(confirmed.reviewedByOrganizerUserId, Some(UserId("organizer-1")))
  }
