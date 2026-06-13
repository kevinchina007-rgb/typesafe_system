// TourGroupReferenceDataSeederSeeds handles demo user and group construction.
package com.typesafe.travel.persistence

import cats.effect.IO
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*

import java.time.LocalDate

object TourGroupReferenceDataSeederSeeds:
  import TourGroupReferenceDataSeederData.*
  import TourGroupReferenceDataSeederWrites.*
  def buildUserSeeds(): Vector[DemoUserSeed] =
    Vector.tabulate(20) { index =>
      val userNo = index + 1
      val userId = f"$DemoUserPrefix$userNo%03d"
      val email = s"$userId$DemoUserEmailDomain"
      val phone = f"1880001$userNo%04d"
      val nickname = f"旅团用户$userNo%02d"
      val fullName = f"旅客$userNo%02d"
      val avatarUrl = s"/images/avatar-defaults/bara-avatar-${1 + (index % 8)}.png"
      val birthDate =
        if index < 12 then LocalDate.of(1988 + (index % 6), 1 + (index % 12), 1 + (index % 20))
        else if index < 16 then LocalDate.of(2010 - (index % 4), 2 + (index % 8), 3 + (index % 18))
        else LocalDate.of(1949 + (index % 8), 3 + (index % 6), 4 + (index % 18))
      val travelerType =
        if index < 12 then "AdultTraveler"
        else if index < 16 then "ChildTraveler"
        else "AdultTraveler"
      val documentNumber = f"TG-DOC-$userNo%04d"
      val documentExpiryDate =
        if index < 16 then LocalDate.of(2035, 12, 31) else LocalDate.of(2031, 12, 31)
      val gender = if index % 2 == 0 then "男" else "女"
      val points = 1000L + index.toLong * 37L
      DemoUserSeed(
        userId = userId,
        email = email,
        phone = phone,
        nickname = nickname,
        fullName = fullName,
        avatarUrl = avatarUrl,
        birthDate = birthDate,
        travelerType = travelerType,
        documentNumber = documentNumber,
        documentExpiryDate = documentExpiryDate,
        gender = gender,
        points = points
      )
    }

  def seedGroups(transactor: Transactor[IO], users: Vector[SeededDemoUser]): IO[Unit] =
    buildGroupSeeds(users).traverse_(seedGroup).transact(transactor).void

  def backfillGroupPlanItems(transactor: Transactor[IO], users: Vector[SeededDemoUser]): IO[Unit] =
    buildGroupSeeds(users).traverse_(seedGroupPlanItemsOnly).transact(transactor).void

  def buildGroupSeeds(users: Vector[SeededDemoUser]): Vector[GroupSeed] =
    val groups = Vector.newBuilder[GroupSeed]
    var groupIndex = 0
    users.zipWithIndex.foreach { case (organizer, organizerIndex) =>
      val groupCount = if organizerIndex < 10 then 5 else 4
      (0 until groupCount).foreach { _ =>
        val template = journeyTemplates(groupIndex % journeyTemplates.size)
        val members = Vector(
          organizer,
          users((organizerIndex + 1) % users.size),
          users((organizerIndex + 2) % users.size),
          users((organizerIndex + 3) % users.size)
        ).distinctBy(_.userId)
        val startDate = BaseStartDate.plusDays((groupIndex % StartDateSpanDays).toLong)
        val endDate = startDate.plusDays(4)
        val createdAt = SeededAt.plusSeconds(groupIndex.toLong * 1800L)
        val capacity = 12 + (groupIndex % 7)
        val allowMemberDirectChat = groupIndex % 2 == 0
        groups += GroupSeed(
          groupIndex = groupIndex,
          groupId = f"$DemoGroupPrefix${groupIndex + 1}%03d",
          organizer = organizer,
          members = members,
          template = template,
          startDate = startDate,
          endDate = endDate,
          capacity = capacity,
          allowMemberDirectChat = allowMemberDirectChat,
          createdAt = createdAt
        )
        groupIndex += 1
      }
    }
    groups.result()

  def seedGroup(group: GroupSeed): ConnectionIO[Unit] =
    val planItems = buildPlanItems(group)
    val publicConversationId = s"conversation-${group.groupId}-public"
    val organizerMember = group.members.head
    val firstMember = group.members.lift(1).getOrElse(group.members.head)
    val secondMember = group.members.lift(2).getOrElse(firstMember)
    val publicMessages = Vector(
      (s"message-${group.groupId}-public-1", organizerMember.userId, s"欢迎加入《${groupTitle(group)}》！这次行程会把 ${group.template.city1.cityName} 和 ${group.template.city2.cityName} 串起来，群里有问题随时说。"),
      (s"message-${group.groupId}-public-2", firstMember.userId, "收到，我先看行程和可选票型。"),
      (s"message-${group.groupId}-public-3", secondMember.userId, "我负责盯紧酒店和返程航班。")
    )
    for
      _ <- upsertTourGroup(group)
      _ <- group.members.zipWithIndex.traverse_ { case (member, memberIndex) =>
        upsertMembership(group, member, memberIndex)
      }
      _ <- group.members.zipWithIndex.traverse_ { case (member, memberIndex) =>
        upsertMembershipTraveler(group, member, memberIndex)
      }
      _ <- upsertChatSettings(group)
      _ <- upsertConversation(
        conversationId = publicConversationId,
        groupId = group.groupId,
        conversationType = "GroupPublic",
        directMemberAUserId = None,
        directMemberBUserId = None,
        createdAt = group.createdAt.plusSeconds(60)
      )
      _ <- group.members.zipWithIndex.traverse_ { case (member, memberIndex) =>
        upsertConversationParticipant(
          participantId = publicParticipantId(publicConversationId, member.userId),
          conversationId = publicConversationId,
          userId = member.userId,
          role = if memberIndex == 0 then "Organizer" else "Member",
          joinedAt = group.createdAt.plusSeconds(90 + memberIndex.toLong * 10L)
        )
      }
      _ <- publicMessages.zipWithIndex.traverse_ { case ((messageId, senderUserId, content), messageIndex) =>
        upsertMessage(
          messageId = messageId,
          conversationId = publicConversationId,
          senderUserId = senderUserId,
          content = content,
          createdAt = group.createdAt.plusSeconds(120 + messageIndex.toLong * 90L)
        )
      }
      _ <- upsertConversation(
        conversationId = directConversationId(group.groupId, organizerMember.userId, firstMember.userId),
        groupId = group.groupId,
        conversationType = "Direct",
        directMemberAUserId = Some(normalizeDirectMemberA(organizerMember.userId, firstMember.userId)),
        directMemberBUserId = Some(normalizeDirectMemberB(organizerMember.userId, firstMember.userId)),
        createdAt = group.createdAt.plusSeconds(180)
      )
      _ <- upsertConversationParticipant(
        participantId = directParticipantId(group.groupId, organizerMember.userId, firstMember.userId, organizerMember.userId),
        conversationId = directConversationId(group.groupId, organizerMember.userId, firstMember.userId),
        userId = organizerMember.userId,
        role = "Organizer",
        joinedAt = group.createdAt.plusSeconds(190)
      )
      _ <- upsertConversationParticipant(
        participantId = directParticipantId(group.groupId, organizerMember.userId, firstMember.userId, firstMember.userId),
        conversationId = directConversationId(group.groupId, organizerMember.userId, firstMember.userId),
        userId = firstMember.userId,
        role = "Member",
        joinedAt = group.createdAt.plusSeconds(200)
      )
      _ <- upsertMessage(
        messageId = s"message-${group.groupId}-direct-1",
        conversationId = directConversationId(group.groupId, organizerMember.userId, firstMember.userId),
        senderUserId = organizerMember.userId,
        content = "你们到站后先在酒店大厅集合，出发前我会再群里确认时间。",
        createdAt = group.createdAt.plusSeconds(230)
      )
      _ <- if group.allowMemberDirectChat then
        val memberConversationId =
          directConversationId(group.groupId, firstMember.userId, secondMember.userId)
        for
          _ <- upsertConversation(
            conversationId = memberConversationId,
            groupId = group.groupId,
            conversationType = "Direct",
            directMemberAUserId = Some(normalizeDirectMemberA(firstMember.userId, secondMember.userId)),
            directMemberBUserId = Some(normalizeDirectMemberB(firstMember.userId, secondMember.userId)),
            createdAt = group.createdAt.plusSeconds(240)
          )
          _ <- upsertConversationParticipant(
            participantId = directParticipantId(group.groupId, firstMember.userId, secondMember.userId, firstMember.userId),
            conversationId = memberConversationId,
            userId = firstMember.userId,
            role = "Member",
            joinedAt = group.createdAt.plusSeconds(250)
          )
          _ <- upsertConversationParticipant(
            participantId = directParticipantId(group.groupId, firstMember.userId, secondMember.userId, secondMember.userId),
            conversationId = memberConversationId,
            userId = secondMember.userId,
            role = "Member",
            joinedAt = group.createdAt.plusSeconds(260)
          )
          _ <- upsertMessage(
            messageId = s"message-${group.groupId}-direct-member-1",
            conversationId = memberConversationId,
            senderUserId = firstMember.userId,
            content = "这条路线如果允许私聊，我们可以单独对一下集合和分房。",
            createdAt = group.createdAt.plusSeconds(270)
          )
        yield ()
      else ().pure[ConnectionIO]
      _ <- planItems.traverse_(upsertPlanItem(group))
    yield ()

  def seedGroupPlanItemsOnly(group: GroupSeed): ConnectionIO[Unit] =
    buildPlanItems(group).traverse_(upsertPlanItem(group))

  def buildPlanItems(group: GroupSeed): Vector[PlanItemSeed] =
    val day1 = group.startDate
    val day2 = day1.plusDays(1)
    val day3 = day1.plusDays(2)
    val day4 = day1.plusDays(3)
    val day5 = day1.plusDays(4)
    val firstHotelId = group.template.city1.hotelIds(group.groupIndex % group.template.city1.hotelIds.size)
    val secondHotelId = group.template.city2.hotelIds((group.groupIndex + 1) % group.template.city2.hotelIds.size)
    val firstAttractionSuffix = group.template.city1.attractionSuffixes(group.groupIndex % group.template.city1.attractionSuffixes.size)
    val secondAttractionSuffix = group.template.city2.attractionSuffixes((group.groupIndex + 2) % group.template.city2.attractionSuffixes.size)
    val firstAttractionId = attractionId(group.template.city1, firstAttractionSuffix)
    val secondAttractionId = attractionId(group.template.city2, secondAttractionSuffix)
    Vector(
      PlanItemSeed(
        itemType = "Flight",
        title = s"${group.template.homeCity.cityName} -> ${group.template.city1.cityName} 航班",
        description = s"从 ${group.template.homeCity.cityName} 前往 ${group.template.city1.cityName}，两种舱位可选。",
        scheduledAt = localInstant(day1, 9, 0),
        endsAt = Some(localInstant(day1, 11, 30)),
        sequenceNo = 1,
        options = Vector(
          PlanOptionSeed(
            resourceType = "Flight",
            resourceId = flightDemoId(group.template.homeCity.cityName, group.template.city1.cityName, day1, 8, 1),
            resourceVariantCode = Some("ECONOMY"),
            resourceContext = Some(s"${group.template.outboundDepartureAirport}|${group.template.outboundArrivalAirport}|$day1"),
            label = "经济舱",
            description = s"${group.template.homeCity.cityName} -> ${group.template.city1.cityName}，适合控制预算。",
            defaultQuantity = 1
          ),
          PlanOptionSeed(
            resourceType = "Flight",
            resourceId = flightDemoId(group.template.homeCity.cityName, group.template.city1.cityName, day1, 8, 1),
            resourceVariantCode = Some("PREMIUM_ECONOMY"),
            resourceContext = Some(s"${group.template.outboundDepartureAirport}|${group.template.outboundArrivalAirport}|$day1"),
            label = "高端经济舱",
            description = s"${group.template.homeCity.cityName} -> ${group.template.city1.cityName}，更舒适的机上体验。",
            defaultQuantity = 1
          )
        )
      ),
      PlanItemSeed(
        itemType = "Hotel",
        title = s"${group.template.city1.cityName} 住宿",
        description = s"入住 ${group.template.city1.cityName}，覆盖到 ${day3} 的中转安排。",
        scheduledAt = localInstant(day1, 15, 0),
        endsAt = Some(localInstant(day3, 12, 0)),
        sequenceNo = 2,
        options = Vector(
          PlanOptionSeed(
            resourceType = "HotelRoomType",
            resourceId = roomTypeId(firstHotelId, "standard"),
            resourceVariantCode = Some("STANDARD"),
            resourceContext = Some(s"${day1}T14:00|${day3}T12:00"),
            label = "标准间",
            description = s"${group.template.city1.cityName} 住宿的经济选择。",
            defaultQuantity = 1
          ),
          PlanOptionSeed(
            resourceType = "HotelRoomType",
            resourceId = roomTypeId(firstHotelId, "suite"),
            resourceVariantCode = Some("SUITE"),
            resourceContext = Some(s"${day1}T14:00|${day3}T12:00"),
            label = "套房",
            description = s"${group.template.city1.cityName} 住宿的舒适选择。",
            defaultQuantity = 1
          )
        )
      ),
      PlanItemSeed(
        itemType = "Attraction",
        title = s"${group.template.city1.cityName} 城市打卡",
        description = s"${group.template.city1.cityName} 的精选景点安排，包含成人票和儿童票。",
        scheduledAt = localInstant(day2, 10, 0),
        endsAt = Some(localInstant(day2, 18, 0)),
        sequenceNo = 3,
        options = Vector(
          PlanOptionSeed(
            resourceType = "AttractionTicketType",
            resourceId = ticketTypeId(firstAttractionId, "adult"),
            resourceVariantCode = Some("ADULT"),
            resourceContext = Some(firstAttractionId),
            label = "成人票",
            description = s"${attractionNameForSuffix(firstAttractionSuffix)} 成人票。",
            defaultQuantity = 1
          ),
          PlanOptionSeed(
            resourceType = "AttractionTicketType",
            resourceId = ticketTypeId(firstAttractionId, "child"),
            resourceVariantCode = Some("CHILD"),
            resourceContext = Some(firstAttractionId),
            label = "儿童票",
            description = s"${attractionNameForSuffix(firstAttractionSuffix)} 儿童票，18 岁以下可用。",
            defaultQuantity = 1
          )
        )
      ),
      PlanItemSeed(
        itemType = "Train",
        title = s"${group.template.trainRoute.label} 高铁",
        description = s"${group.template.trainRoute.fromCode} -> ${group.template.trainRoute.toCode} 的中段衔接。",
        scheduledAt = localInstant(day3, 8, 0),
        endsAt = Some(localInstant(day3, 12, 0)),
        sequenceNo = 4,
        options = Vector(
          PlanOptionSeed(
            resourceType = "TrainJourneySeat",
            resourceId = group.template.trainRoute.trainId,
            resourceVariantCode = Some("SECOND_CLASS"),
            resourceContext = Some(s"${group.template.trainRoute.fromCode}|${group.template.trainRoute.toCode}|$day3"),
            label = "二等座",
            description = s"${group.template.trainRoute.fromCode} -> ${group.template.trainRoute.toCode}，适合常规出行。",
            defaultQuantity = 1
          ),
          PlanOptionSeed(
            resourceType = "TrainJourneySeat",
            resourceId = group.template.trainRoute.trainId,
            resourceVariantCode = Some("FIRST_CLASS"),
            resourceContext = Some(s"${group.template.trainRoute.fromCode}|${group.template.trainRoute.toCode}|$day3"),
            label = "一等座",
            description = s"${group.template.trainRoute.fromCode} -> ${group.template.trainRoute.toCode}，更舒适的乘坐体验。",
            defaultQuantity = 1
          )
        )
      ),
      PlanItemSeed(
        itemType = "Hotel",
        title = s"${group.template.city2.cityName} 住宿",
        description = s"入住 ${group.template.city2.cityName}，承接后半段安排。",
        scheduledAt = localInstant(day3, 15, 0),
        endsAt = Some(localInstant(day5, 12, 0)),
        sequenceNo = 5,
        options = Vector(
          PlanOptionSeed(
            resourceType = "HotelRoomType",
            resourceId = roomTypeId(secondHotelId, "standard"),
            resourceVariantCode = Some("STANDARD"),
            resourceContext = Some(s"${day3}T14:00|${day5}T12:00"),
            label = "标准间",
            description = s"${group.template.city2.cityName} 住宿的标准房型。",
            defaultQuantity = 1
          ),
          PlanOptionSeed(
            resourceType = "HotelRoomType",
            resourceId = roomTypeId(secondHotelId, "twin"),
            resourceVariantCode = Some("TWIN"),
            resourceContext = Some(s"${day3}T14:00|${day5}T12:00"),
            label = "双床房",
            description = s"${group.template.city2.cityName} 住宿的双床房型。",
            defaultQuantity = 1
          )
        )
      ),
      PlanItemSeed(
        itemType = "Attraction",
        title = s"${group.template.city2.cityName} 城市打卡",
        description = s"${group.template.city2.cityName} 的精选景点安排，包含成人票和长者票。",
        scheduledAt = localInstant(day4, 10, 0),
        endsAt = Some(localInstant(day4, 18, 0)),
        sequenceNo = 6,
        options = Vector(
          PlanOptionSeed(
            resourceType = "AttractionTicketType",
            resourceId = ticketTypeId(secondAttractionId, "adult"),
            resourceVariantCode = Some("ADULT"),
            resourceContext = Some(secondAttractionId),
            label = "成人票",
            description = s"${attractionNameForSuffix(secondAttractionSuffix)} 成人票。",
            defaultQuantity = 1
          ),
          PlanOptionSeed(
            resourceType = "AttractionTicketType",
            resourceId = ticketTypeId(secondAttractionId, "senior"),
            resourceVariantCode = Some("SENIOR"),
            resourceContext = Some(secondAttractionId),
            label = "长者票",
            description = s"${attractionNameForSuffix(secondAttractionSuffix)} 长者票，70 岁及以上可用。",
            defaultQuantity = 1
          )
        )
      ),
      PlanItemSeed(
        itemType = "Flight",
        title = s"${group.template.city2.cityName} -> ${group.template.homeCity.cityName} 回程航班",
        description = s"从 ${group.template.city2.cityName} 返回 ${group.template.homeCity.cityName}，安排在行程最后一天。",
        scheduledAt = localInstant(day5, 17, 0),
        endsAt = Some(localInstant(day5, 20, 0)),
        sequenceNo = 7,
        options = Vector(
          PlanOptionSeed(
            resourceType = "Flight",
            resourceId = flightDemoId(group.template.city2.cityName, group.template.homeCity.cityName, day5, 20, 1),
            resourceVariantCode = Some("BUSINESS"),
            resourceContext = Some(s"${group.template.returnDepartureAirport}|${group.template.returnArrivalAirport}|$day5"),
            label = "商务舱",
            description = s"${group.template.city2.cityName} -> ${group.template.homeCity.cityName}，适合舒适返程。",
            defaultQuantity = 1
          ),
          PlanOptionSeed(
            resourceType = "Flight",
            resourceId = flightDemoId(group.template.city2.cityName, group.template.homeCity.cityName, day5, 20, 1),
            resourceVariantCode = Some("FIRST"),
            resourceContext = Some(s"${group.template.returnDepartureAirport}|${group.template.returnArrivalAirport}|$day5"),
            label = "头等舱",
            description = s"${group.template.city2.cityName} -> ${group.template.homeCity.cityName}，最舒适的返程选择。",
            defaultQuantity = 1
          )
        )
      )
    )
