package com.typesafe.travel.persistence.tourgroup

import cats.effect.unsafe.implicits.global
import com.typesafe.travel.persistence.{PersistenceTestSupport, SchemaInitializer}
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.tourgroup.domain.*
import munit.FunSuite

import java.time.{Instant, LocalDate}

final class DoobieTourGroupRepositorySpec extends FunSuite:
  test("tour group round trip restores derived capacity inputs") {
    val transactor = PersistenceTestSupport.createTestTransactor()
    SchemaInitializer.initialize(transactor).unsafeRunSync()
    val repository = DoobieTourGroupRepository[cats.effect.IO](transactor)

    val createdAt = Instant.parse("2026-03-30T09:00:00Z")
    val group = TourGroup.create(
      groupId = TourGroupId("group-rt-1"),
      organizerUserId = UserId("user-1"),
      title = "Yangtze Tour",
      description = "A short group trip",
      destination = "Chongqing",
      startDate = LocalDate.parse("2026-05-01"),
      endDate = LocalDate.parse("2026-05-05"),
      capacity = 6,
      createdAt = createdAt
    ).toOption.get
    val membership = TourGroupMembership.createOrganizerMembership(TourGroupMembershipId("membership-1"), group.groupId, group.organizerUserId, createdAt)
    val membershipTraveler = TourGroupMembershipTraveler(TourGroupMembershipTravelerId("membership-traveler-1"), membership.membershipId, TravelerId("traveler-1"), createdAt, TourGroupMembershipTravelerStatus.Active)
    val planItem = GroupPlanItem.create(
      planItemId = GroupPlanItemId("plan-item-1"),
      groupId = group.groupId,
      itemType = GroupPlanItemType.Flight,
      title = "Outbound flight",
      description = "Choose a flight",
      scheduledAt = createdAt,
      endsAt = Some(createdAt.plusSeconds(3600)),
      sequenceNo = 1
    ).toOption.get
    val option = GroupPlanOption.create(
      optionId = GroupPlanOptionId("option-1"),
      planItemId = planItem.planItemId,
      resourceType = GroupPlanOptionResourceType.Flight,
      resourceId = "flight-1",
      resourceVariantCode = Some("economy"),
      resourceContext = None,
      label = "MU123",
      description = "Morning flight",
      defaultQuantity = 1
    ).toOption.get
    val selection = GroupPlanSelection.create(
      selectionId = GroupPlanSelectionId("selection-1"),
      groupId = group.groupId,
      planItemId = planItem.planItemId,
      optionId = option.optionId,
      membershipId = membership.membershipId,
      quantity = 1,
      createdAt = createdAt
    ).toOption.get.submit.toOption.get.confirm(UserId("user-1"), createdAt.plusSeconds(60), Some("confirmed")).toOption.get
    val selectionTraveler = GroupPlanSelectionTraveler(GroupPlanSelectionTravelerId("selection-traveler-1"), selection.selectionId, membershipTraveler.travelerId)
    val link = GroupSelectionOrderLink(GroupSelectionOrderLinkId("link-1"), selection.selectionId, OrderId("order-1"), createdAt.plusSeconds(120))

    repository.saveGroup(group).unsafeRunSync()
    repository.saveMembership(membership).unsafeRunSync()
    repository.saveMembershipTraveler(membershipTraveler).unsafeRunSync()
    repository.savePlanItem(planItem).unsafeRunSync()
    repository.savePlanOption(option).unsafeRunSync()
    repository.saveSelection(selection).unsafeRunSync()
    repository.saveSelectionTraveler(selectionTraveler).unsafeRunSync()
    repository.saveSelectionOrderLink(link).unsafeRunSync()

    val restored = repository.findGroupDetails(group.groupId).unsafeRunSync()

    assert(restored.nonEmpty)
    assertEquals(restored.get.usedCapacity, 1)
    assertEquals(restored.get.selections.head.status, GroupPlanSelectionStatus.OrganizerConfirmed)
    assertEquals(restored.get.selectionOrderLinks.head.orderId, OrderId("order-1"))
  }
