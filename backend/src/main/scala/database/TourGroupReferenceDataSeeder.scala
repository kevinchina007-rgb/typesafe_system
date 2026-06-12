// TourGroupReferenceDataSeeder 负责写入参考数据。

package com.typesafe.travel.persistence

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.shared.kernel.EmailAddress
import doobie.*
import doobie.implicits.*
import doobie.util.fragment.Fragment

import java.security.MessageDigest
import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate, ZoneOffset}
import java.util.HexFormat

object TourGroupReferenceDataSeeder:
  private val DemoPassword = "12345qwert"
  private val DemoUserPrefix = "tour-user-"
  private val DemoUserEmailDomain = "@demo.local"
  private val DemoGroupPrefix = "tour-group-demo-"
  private val SeededAt = Instant.parse("2026-05-18T00:00:00Z")
  private val BaseStartDate = LocalDate.parse("2026-06-01")
  private val StartDateSpanDays = 57L
  private val ExpectedDemoGroupCount = 90L

  private def sqlStringLiteral(value: String): Fragment =
    Fragment.const("'" + value.replace("'", "''") + "'")

  private def sqlOptionalStringLiteral(value: Option[String]): Fragment =
    value match
      case Some(text) => sqlStringLiteral(text)
      case None       => Fragment.const("null")

  private def sqlTimestampLiteral(value: Timestamp): Fragment =
    Fragment.const("TIMESTAMP '" + value.toLocalDateTime.toString.replace("'", "''") + "'")

  private def sqlOptionalTimestampLiteral(value: Option[Timestamp]): Fragment =
    value match
      case Some(timestamp) => sqlTimestampLiteral(timestamp)
      case None            => Fragment.const("null")

  private def sqlDateLiteral(value: Date): Fragment =
    Fragment.const("DATE '" + value.toString + "'")

  private final case class DemoUserSeed(
      userId: String,
      email: String,
      phone: String,
      nickname: String,
      fullName: String,
      avatarUrl: String,
      birthDate: LocalDate,
      travelerType: String,
      documentNumber: String,
      documentExpiryDate: LocalDate,
      gender: String,
      points: Long
  )

  private final case class SeededDemoUser(
      userId: String,
      email: String,
      nickname: String,
      travelerId: String,
      birthDate: LocalDate
  )

  private final case class CityResource(
      cityName: String,
      citySlug: String,
      primaryAirport: String,
      secondaryAirport: Option[String],
      hotelIds: Vector[String],
      attractionSuffixes: Vector[String]
  )

  private final case class TrainRouteTemplate(
      trainId: String,
      fromCode: String,
      toCode: String,
      label: String
  )

  private final case class JourneyTemplate(
      titlePrefix: String,
      descriptionPrefix: String,
      homeCity: CityResource,
      city1: CityResource,
      city2: CityResource,
      outboundDepartureAirport: String,
      outboundArrivalAirport: String,
      returnDepartureAirport: String,
      returnArrivalAirport: String,
      trainRoute: TrainRouteTemplate
  )

  private final case class GroupSeed(
      groupIndex: Int,
      groupId: String,
      organizer: SeededDemoUser,
      members: Vector[SeededDemoUser],
      template: JourneyTemplate,
      startDate: LocalDate,
      endDate: LocalDate,
      capacity: Int,
      allowMemberDirectChat: Boolean,
      createdAt: Instant
  )

  private final case class PlanOptionSeed(
      resourceType: String,
      resourceId: String,
      resourceVariantCode: Option[String],
      resourceContext: Option[String],
      label: String,
      description: String,
      defaultQuantity: Int
  )

  private final case class PlanItemSeed(
      itemType: String,
      title: String,
      description: String,
      scheduledAt: Instant,
      endsAt: Option[Instant],
      sequenceNo: Int,
      options: Vector[PlanOptionSeed]
  )

  private val cityResources = Vector(
    CityResource(
      cityName = "北京",
      citySlug = "beijing",
      primaryAirport = "PEK",
      secondaryAirport = Some("PKX"),
      hotelIds = Vector("hotel-beijing-guomen", "hotel-beijing-daxing"),
      attractionSuffixes = Vector("museum", "night-tour", "ancient", "science")
    ),
    CityResource(
      cityName = "上海",
      citySlug = "shanghai",
      primaryAirport = "SHA",
      secondaryAirport = Some("PVG"),
      hotelIds = Vector("hotel-sh-bund", "hotel-shanghai-hongqiao"),
      attractionSuffixes = Vector("museum", "garden", "theme-park", "night-tour")
    ),
    CityResource(
      cityName = "杭州",
      citySlug = "hangzhou",
      primaryAirport = "HGH",
      secondaryAirport = None,
      hotelIds = Vector("hotel-hz-westlake", "hotel-hangzhou-qiantang"),
      attractionSuffixes = Vector("garden", "museum", "night-tour", "science")
    ),
    CityResource(
      cityName = "广州",
      citySlug = "guangzhou",
      primaryAirport = "CAN",
      secondaryAirport = None,
      hotelIds = Vector("hotel-guangzhou-pearl", "hotel-guangzhou-baiyun"),
      attractionSuffixes = Vector("theme-park", "ocean-park", "museum", "night-tour")
    ),
    CityResource(
      cityName = "厦门",
      citySlug = "xiamen",
      primaryAirport = "XMN",
      secondaryAirport = None,
      hotelIds = Vector("hotel-xiamen-gulangyu", "hotel-xiamen-bay"),
      attractionSuffixes = Vector("art", "night-tour", "garden", "ancient")
    ),
    CityResource(
      cityName = "成都",
      citySlug = "chengdu",
      primaryAirport = "CTU",
      secondaryAirport = Some("TFU"),
      hotelIds = Vector("hotel-chengdu-tianfu", "hotel-chengdu-jinjiang"),
      attractionSuffixes = Vector("science", "museum", "theme-park", "ancient")
    ),
    CityResource(
      cityName = "长沙",
      citySlug = "changsha",
      primaryAirport = "CSX",
      secondaryAirport = None,
      hotelIds = Vector("hotel-changsha-yuelu", "hotel-changsha-xiangjiang"),
      attractionSuffixes = Vector("museum", "theme-park", "night-tour", "art")
    ),
    CityResource(
      cityName = "重庆",
      citySlug = "chongqing",
      primaryAirport = "CKG",
      secondaryAirport = None,
      hotelIds = Vector("hotel-chongqing-shancheng", "hotel-chongqing-jiangjing"),
      attractionSuffixes = Vector("night-tour", "art", "museum", "garden")
    ),
    CityResource(
      cityName = "西安",
      citySlug = "xian",
      primaryAirport = "XIY",
      secondaryAirport = None,
      hotelIds = Vector("hotel-xian-gudu", "hotel-xian-changan"),
      attractionSuffixes = Vector("ancient", "museum", "night-tour", "art")
    )
  )

  private val trainRoutes = Vector(
    TrainRouteTemplate("train-hh306-h1001", "BJS", "SHH", "京沪高铁"),
    TrainRouteTemplate("train-hh306-h1002", "SHH", "WZS", "沪温动车"),
    TrainRouteTemplate("train-hh306-h1003", "GZQ", "XMN", "广厦城际"),
    TrainRouteTemplate("train-hh306-h1004", "CDD", "CSN", "成长高铁"),
    TrainRouteTemplate("train-hh306-h1005", "BJS", "GZQ", "京广高铁")
  )

  private val journeyTemplates = Vector(
    JourneyTemplate(
      titlePrefix = "京沪杭慢游团",
      descriptionPrefix = "适合喜欢城市漫游与轻松节奏的游客",
      homeCity = cityResource("北京"),
      city1 = cityResource("上海"),
      city2 = cityResource("杭州"),
      outboundDepartureAirport = "PEK",
      outboundArrivalAirport = "SHA",
      returnDepartureAirport = "HGH",
      returnArrivalAirport = "PEK",
      trainRoute = trainRoutes(0)
    ),
    JourneyTemplate(
      titlePrefix = "沪穗厦海岛团",
      descriptionPrefix = "把城市美食、海岸风景和夜游行程一次串起来",
      homeCity = cityResource("上海"),
      city1 = cityResource("广州"),
      city2 = cityResource("厦门"),
      outboundDepartureAirport = "SHA",
      outboundArrivalAirport = "CAN",
      returnDepartureAirport = "XMN",
      returnArrivalAirport = "PVG",
      trainRoute = trainRoutes(2)
    ),
    JourneyTemplate(
      titlePrefix = "京蓉星城团",
      descriptionPrefix = "适合喜欢美食、火锅和轻松打卡的组合路线",
      homeCity = cityResource("北京"),
      city1 = cityResource("成都"),
      city2 = cityResource("长沙"),
      outboundDepartureAirport = "PEK",
      outboundArrivalAirport = "CTU",
      returnDepartureAirport = "CSX",
      returnArrivalAirport = "PEK",
      trainRoute = trainRoutes(3)
    ),
    JourneyTemplate(
      titlePrefix = "南方山城古都团",
      descriptionPrefix = "将山城夜景和古都文化放在同一条线上",
      homeCity = cityResource("广州"),
      city1 = cityResource("重庆"),
      city2 = cityResource("西安"),
      outboundDepartureAirport = "CAN",
      outboundArrivalAirport = "CKG",
      returnDepartureAirport = "XIY",
      returnArrivalAirport = "CAN",
      trainRoute = trainRoutes(4)
    )
  )

  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    for
      _ <- ensureTourGroupColumns(transactor)
      _ <- ensureBlacklistsTable(transactor)
      demoGroupCount <- sql"select count(*) from tour_groups where group_id like 'tour-group-demo-%'".query[Long].unique.transact(transactor)
      _ <- if demoGroupCount >= ExpectedDemoGroupCount then IO.unit else seedDemoData(transactor)
    yield ()

  private def seedDemoData(transactor: Transactor[IO]): IO[Unit] =
    for
      _ <- cleanupDemoData(transactor)
      userSeeds = buildUserSeeds()
      hashedUsers <- userSeeds.traverse { seed =>
        hashPasswordForLoginEmail(DemoPassword, EmailAddress.unsafe(seed.email)).map(passwordHash => seed -> passwordHash)
      }
      seededUsers <- hashedUsers.traverse { case (seed, passwordHash) => upsertDemoUser(seed, passwordHash) }.transact(transactor)
      _ <- seedGroups(transactor, seededUsers)
      _ <- backfillGroupPlanItems(transactor, seededUsers)
    yield ()

  private def ensureTourGroupColumns(transactor: Transactor[IO]): IO[Unit] =
    List(
      sql"""
        alter table tour_groups
          add column if not exists cover_image_url varchar(512)
      """.update.run.void,
      sql"""
        alter table tour_groups
          add column if not exists tags_json text not null default '[]'
      """.update.run.void
    ).sequence_.transact(transactor)

  private def ensureBlacklistsTable(transactor: Transactor[IO]): IO[Unit] =
    List(
      sql"""
        create table if not exists tour_group_blacklists (
          blacklist_id varchar(64) primary key,
          group_id varchar(64) not null,
          user_id varchar(64) not null,
          blacklisted_by_user_id varchar(64) not null,
          reason varchar(500) not null,
          created_at timestamp not null
        )
      """.update.run.void,
      sql"""
        create unique index if not exists idx_tour_group_blacklists_unique
          on tour_group_blacklists(group_id, user_id)
      """.update.run.void,
      sql"""
        create index if not exists idx_tour_group_blacklists_user_id
          on tour_group_blacklists(user_id)
      """.update.run.void
    ).sequence_.transact(transactor)

  private def cleanupDemoData(transactor: Transactor[IO]): IO[Unit] =
    List(
      sql"""
        delete from tour_group_message_reactions
        where message_id in (
          select m.message_id
          from tour_group_messages m
          inner join tour_group_conversations c on c.conversation_id = m.conversation_id
          where c.group_id like ${s"$DemoGroupPrefix%"}
        )
      """.update.run.void,
      sql"""
        delete from tour_group_message_attachments
        where message_id in (
          select m.message_id
          from tour_group_messages m
          inner join tour_group_conversations c on c.conversation_id = m.conversation_id
          where c.group_id like ${s"$DemoGroupPrefix%"}
        )
      """.update.run.void,
      sql"""
        delete from tour_group_messages
        where conversation_id in (
          select conversation_id
          from tour_group_conversations
          where group_id like ${s"$DemoGroupPrefix%"}
        )
      """.update.run.void,
      sql"""
        delete from tour_group_conversation_participants
        where conversation_id in (
          select conversation_id
          from tour_group_conversations
          where group_id like ${s"$DemoGroupPrefix%"}
        )
      """.update.run.void,
      sql"""
        delete from tour_group_conversations
        where group_id like ${s"$DemoGroupPrefix%"}
      """.update.run.void,
      sql"""
        delete from tour_group_chat_settings
        where group_id like ${s"$DemoGroupPrefix%"}
      """.update.run.void,
      sql"""
        delete from group_selection_order_links
        where selection_id in (
          select selection_id
          from group_plan_selections
          where group_id like ${s"$DemoGroupPrefix%"}
        )
      """.update.run.void,
      sql"""
        delete from group_plan_selection_travelers
        where selection_id in (
          select selection_id
          from group_plan_selections
          where group_id like ${s"$DemoGroupPrefix%"}
        )
      """.update.run.void,
      sql"""
        delete from group_plan_selections
        where group_id like ${s"$DemoGroupPrefix%"}
      """.update.run.void,
      sql"""
        delete from group_plan_options
        where plan_item_id in (
          select plan_item_id
          from group_plan_items
          where group_id like ${s"$DemoGroupPrefix%"}
        )
      """.update.run.void,
      sql"""
        delete from group_plan_items
        where group_id like ${s"$DemoGroupPrefix%"}
      """.update.run.void,
      sql"""
        delete from tour_group_blacklists
        where group_id like ${s"$DemoGroupPrefix%"}
      """.update.run.void,
      sql"""
        delete from tour_group_membership_travelers
        where membership_id in (
          select membership_id
          from tour_group_memberships
          where group_id like ${s"$DemoGroupPrefix%"}
        )
      """.update.run.void,
      sql"""
        delete from tour_group_memberships
        where group_id like ${s"$DemoGroupPrefix%"}
      """.update.run.void,
      sql"""
        delete from tour_groups
        where group_id like ${s"$DemoGroupPrefix%"}
      """.update.run.void,
      sql"""
        delete from auth_sessions
        where actor_type = ${"User"}
          and actor_id like ${s"$DemoUserPrefix%"}
      """.update.run.void,
      sql"""
        delete from user_credentials
        where login_email like ${s"$DemoUserPrefix%$DemoUserEmailDomain"}
      """.update.run.void,
      sql"""
        delete from traveler_profiles
        where owner_user_id in (
          select user_id
          from users
          where email like ${s"$DemoUserPrefix%$DemoUserEmailDomain"}
        )
      """.update.run.void,
      sql"""
        delete from users
        where email like ${s"$DemoUserPrefix%$DemoUserEmailDomain"}
      """.update.run.void
    ).sequence.transact(transactor).void

  private def buildUserSeeds(): Vector[DemoUserSeed] =
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

  private def seedGroups(transactor: Transactor[IO], users: Vector[SeededDemoUser]): IO[Unit] =
    buildGroupSeeds(users).traverse_(seedGroup).transact(transactor).void

  private def backfillGroupPlanItems(transactor: Transactor[IO], users: Vector[SeededDemoUser]): IO[Unit] =
    buildGroupSeeds(users).traverse_(seedGroupPlanItemsOnly).transact(transactor).void

  private def buildGroupSeeds(users: Vector[SeededDemoUser]): Vector[GroupSeed] =
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

  private def seedGroup(group: GroupSeed): ConnectionIO[Unit] =
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

  private def seedGroupPlanItemsOnly(group: GroupSeed): ConnectionIO[Unit] =
    buildPlanItems(group).traverse_(upsertPlanItem(group))

  private def buildPlanItems(group: GroupSeed): Vector[PlanItemSeed] =
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

  private def upsertDemoUser(seed: DemoUserSeed, passwordHash: String): ConnectionIO[SeededDemoUser] =
    val travelerId = s"traveler-${seed.userId}"
    val createdAt = Timestamp.from(SeededAt.plusSeconds(seed.points))
    val createdAtSql = sqlTimestampLiteral(createdAt)
    for
      _ <- sql"""
        insert into users(
          user_id, email, phone, nickname, avatar_url, membership_level, points, status, default_traveler_id, created_at
        ) values (
          ${seed.userId}, ${seed.email}, ${seed.phone}, ${seed.nickname}, ${seed.avatarUrl},
          ${"Basic"}, ${seed.points}, ${"Active"}, ${travelerId}, $createdAtSql
        )
        on conflict (user_id) do update set
          email = excluded.email,
          phone = excluded.phone,
          nickname = excluded.nickname,
          avatar_url = excluded.avatar_url,
          membership_level = excluded.membership_level,
          points = excluded.points,
          status = excluded.status,
          default_traveler_id = excluded.default_traveler_id,
          created_at = excluded.created_at
      """.update.run
      _ <- sql"""
        insert into user_credentials(
          credential_id, user_id, login_email, password_hash, status, created_at, updated_at, password_updated_at
        ) values (
          ${s"credential-${seed.userId}"}, ${seed.userId}, ${seed.email}, $passwordHash,
          ${"Active"}, $createdAtSql, $createdAtSql, $createdAtSql
        )
        on conflict (login_email) do update set
          user_id = excluded.user_id,
          password_hash = excluded.password_hash,
          status = excluded.status,
          updated_at = excluded.updated_at,
          password_updated_at = excluded.password_updated_at
      """.update.run
      _ <- sql"""
        insert into traveler_profiles(
          traveler_id, owner_user_id, full_name, birth_date, document_type, document_number, phone,
          traveler_type, status, is_default, preferences_json, emergency_contact_json,
          identity_documents_json, loyalty_memberships_json, gender, nationality, document_expiry_date,
          email, quiet_seat_preferred, assistance_type, special_requirement_note, has_large_luggage, luggage_note
        ) values (
          $travelerId, ${seed.userId}, ${seed.fullName}, ${sqlDateLiteral(Date.valueOf(seed.birthDate))}, ${"Passport"}, ${seed.documentNumber}, ${seed.phone},
          ${seed.travelerType}, ${"Verified"}, ${true},
          ${"""{"travelerSeatPreference":"NoPreference","travelerMealPreference":"NoPreference","accessibilityRequestNotes":null}"""},
          ${sqlOptionalStringLiteral(None)},
          ${s"""[{"travelerDocumentType":"Passport","travelerDocumentNumber":"${seed.documentNumber}","issuingCountryCode":"CN","expirationDate":"${seed.documentExpiryDate}"}]"""},
          ${"[]"},
          ${seed.gender}, ${"中国"}, ${sqlDateLiteral(Date.valueOf(seed.documentExpiryDate))},
          ${seed.email}, ${false}, ${"无"}, ${sqlOptionalStringLiteral(None)}, ${false}, ${sqlOptionalTimestampLiteral(None)}
        )
        on conflict (traveler_id) do update set
          owner_user_id = excluded.owner_user_id,
          full_name = excluded.full_name,
          birth_date = excluded.birth_date,
          document_type = excluded.document_type,
          document_number = excluded.document_number,
          phone = excluded.phone,
          traveler_type = excluded.traveler_type,
          status = excluded.status,
          is_default = excluded.is_default,
          preferences_json = excluded.preferences_json,
          emergency_contact_json = excluded.emergency_contact_json,
          identity_documents_json = excluded.identity_documents_json,
          loyalty_memberships_json = excluded.loyalty_memberships_json,
          gender = excluded.gender,
          nationality = excluded.nationality,
          document_expiry_date = excluded.document_expiry_date,
          email = excluded.email,
          quiet_seat_preferred = excluded.quiet_seat_preferred,
          assistance_type = excluded.assistance_type,
          special_requirement_note = excluded.special_requirement_note,
          has_large_luggage = excluded.has_large_luggage,
          luggage_note = excluded.luggage_note
      """.update.run
      _ <- sql"""
        update users
        set default_traveler_id = $travelerId
        where user_id = ${seed.userId}
      """.update.run
    yield SeededDemoUser(seed.userId, seed.email, seed.nickname, travelerId, seed.birthDate)

  private def upsertTourGroup(group: GroupSeed): ConnectionIO[Int] =
    val createdAt = Timestamp.from(group.createdAt)
    val createdAtSql = sqlTimestampLiteral(createdAt)
    val coverImageUrlSql = sqlOptionalStringLiteral(Some(s"https://picsum.photos/seed/${group.groupId}/1200/800"))
    sql"""
      insert into tour_groups(
        group_id, organizer_user_id, title, description, destination, start_date, end_date, capacity,
        cover_image_url, tags_json, status, created_at
      ) values (
        ${group.groupId}, ${group.organizer.userId}, ${groupTitle(group)}, ${groupDescription(group)}, ${groupDestination(group)},
        ${sqlDateLiteral(Date.valueOf(group.startDate))}, ${sqlDateLiteral(Date.valueOf(group.endDate))}, ${group.capacity},
        $coverImageUrlSql, ${groupTags(group).mkString("[\"", "\", \"", "\"]")},
        ${"Open"}, $createdAtSql
      )
      on conflict (group_id) do update set
        organizer_user_id = excluded.organizer_user_id,
        title = excluded.title,
        description = excluded.description,
        destination = excluded.destination,
        start_date = excluded.start_date,
        end_date = excluded.end_date,
        capacity = excluded.capacity,
        cover_image_url = excluded.cover_image_url,
        tags_json = excluded.tags_json,
        status = excluded.status,
        created_at = excluded.created_at
    """.update.run

  private def upsertMembership(group: GroupSeed, member: SeededDemoUser, index: Int): ConnectionIO[Int] =
    val joinedAt = Timestamp.from(group.createdAt.plusSeconds(30L + index.toLong * 15L))
    val joinedAtSql = sqlTimestampLiteral(joinedAt)
    sql"""
      insert into tour_group_memberships(membership_id, group_id, user_id, joined_at, status)
      values (
        ${membershipId(group.groupId, member.userId)},
        ${group.groupId},
        ${member.userId},
        $joinedAtSql,
        ${"Active"}
      )
      on conflict (membership_id) do update set
        group_id = excluded.group_id,
        user_id = excluded.user_id,
        joined_at = excluded.joined_at,
        status = excluded.status
    """.update.run

  private def upsertMembershipTraveler(group: GroupSeed, member: SeededDemoUser, index: Int): ConnectionIO[Int] =
    val joinedAt = Timestamp.from(group.createdAt.plusSeconds(45L + index.toLong * 15L))
    val joinedAtSql = sqlTimestampLiteral(joinedAt)
    sql"""
      insert into tour_group_membership_travelers(
        membership_traveler_id, membership_id, traveler_id, joined_at, status
      ) values (
        ${membershipTravelerId(group.groupId, member.userId)},
        ${membershipId(group.groupId, member.userId)},
        ${member.travelerId},
        $joinedAtSql,
        ${"Active"}
      )
      on conflict (membership_traveler_id) do update set
        membership_id = excluded.membership_id,
        traveler_id = excluded.traveler_id,
        joined_at = excluded.joined_at,
        status = excluded.status
    """.update.run

  private def upsertChatSettings(group: GroupSeed): ConnectionIO[Int] =
    val updatedAt = Timestamp.from(group.createdAt.plusSeconds(55L))
    val updatedAtSql = sqlTimestampLiteral(updatedAt)
    sql"""
      insert into tour_group_chat_settings(group_id, allow_member_direct_chat, updated_at, updated_by_user_id)
      values (${group.groupId}, ${group.allowMemberDirectChat}, $updatedAtSql, ${group.organizer.userId})
      on conflict (group_id) do update set
        allow_member_direct_chat = excluded.allow_member_direct_chat,
        updated_at = excluded.updated_at,
        updated_by_user_id = excluded.updated_by_user_id
    """.update.run

  private def upsertConversation(
      conversationId: String,
      groupId: String,
      conversationType: String,
      directMemberAUserId: Option[String],
      directMemberBUserId: Option[String],
      createdAt: Instant
  ): ConnectionIO[Int] =
    val createdAtTs = Timestamp.from(createdAt)
    val createdAtSql = sqlTimestampLiteral(createdAtTs)
    sql"""
      insert into tour_group_conversations(
        conversation_id, group_id, conversation_type, status, direct_member_a_user_id, direct_member_b_user_id, created_at, updated_at
      ) values (
        $conversationId, $groupId, $conversationType, ${"Active"}, $directMemberAUserId, $directMemberBUserId, $createdAtSql, $createdAtSql
      )
      on conflict (conversation_id) do update set
        group_id = excluded.group_id,
        conversation_type = excluded.conversation_type,
        status = excluded.status,
        direct_member_a_user_id = excluded.direct_member_a_user_id,
        direct_member_b_user_id = excluded.direct_member_b_user_id,
        created_at = excluded.created_at,
        updated_at = excluded.updated_at
    """.update.run

  private def upsertConversationParticipant(
      participantId: String,
      conversationId: String,
      userId: String,
      role: String,
      joinedAt: Instant
  ): ConnectionIO[Int] =
    val joinedAtTs = Timestamp.from(joinedAt)
    val joinedAtSql = sqlTimestampLiteral(joinedAtTs)
    val emptyTsSql = sqlOptionalTimestampLiteral(None)
    val emptyTextSql = sqlOptionalStringLiteral(None)
    sql"""
      insert into tour_group_conversation_participants(
        participant_id, conversation_id, user_id, role, joined_at, status, last_read_at, last_read_message_id, muted_at, archived_at
      ) values (
        $participantId, $conversationId, $userId, $role, $joinedAtSql, ${"Active"}, $emptyTsSql, $emptyTextSql, $emptyTsSql, $emptyTsSql
      )
      on conflict (conversation_id, user_id) do update set
        participant_id = excluded.participant_id,
        role = excluded.role,
        joined_at = excluded.joined_at,
        status = excluded.status,
        last_read_at = excluded.last_read_at,
        last_read_message_id = excluded.last_read_message_id,
        muted_at = excluded.muted_at,
        archived_at = excluded.archived_at
    """.update.run

  private def upsertMessage(
      messageId: String,
      conversationId: String,
      senderUserId: String,
      content: String,
      createdAt: Instant
  ): ConnectionIO[Int] =
    val createdAtTs = Timestamp.from(createdAt)
    val createdAtSql = sqlTimestampLiteral(createdAtTs)
    val emptyTsSql = sqlOptionalTimestampLiteral(None)
    val emptyTextSql = sqlOptionalStringLiteral(None)
    sql"""
      insert into tour_group_messages(
        message_id, conversation_id, sender_user_id, content, status, created_at,
        message_type, reply_to_message_id, forwarded_from_message_id, updated_at, deleted_at, recalled_at
      ) values (
        $messageId, $conversationId, $senderUserId, $content, ${"Visible"}, $createdAtSql,
        ${"Text"}, $emptyTextSql, $emptyTextSql, $createdAtSql, $emptyTsSql, $emptyTsSql
      )
      on conflict (message_id) do update set
        conversation_id = excluded.conversation_id,
        sender_user_id = excluded.sender_user_id,
        content = excluded.content,
        status = excluded.status,
        created_at = excluded.created_at,
        message_type = excluded.message_type,
        reply_to_message_id = excluded.reply_to_message_id,
        forwarded_from_message_id = excluded.forwarded_from_message_id,
        updated_at = excluded.updated_at,
        deleted_at = excluded.deleted_at,
        recalled_at = excluded.recalled_at
    """.update.run

  private def upsertPlanItem(group: GroupSeed)(planItem: PlanItemSeed): ConnectionIO[Unit] =
    val scheduledAt = Timestamp.from(planItem.scheduledAt)
    val endsAt = planItem.endsAt.map(Timestamp.from)
    val scheduledAtSql = sqlTimestampLiteral(scheduledAt)
    val endsAtSql = sqlOptionalTimestampLiteral(endsAt)
    for
      _ <- sql"""
        insert into group_plan_items(
          plan_item_id, group_id, item_type, title, description, scheduled_at, ends_at, sequence_no, status
        ) values (
          ${planItemId(group.groupId, planItem.sequenceNo)},
          ${group.groupId},
          ${planItem.itemType},
          ${planItem.title},
          ${planItem.description},
          $scheduledAtSql,
          $endsAtSql,
          ${planItem.sequenceNo},
          ${"Open"}
        )
        on conflict (group_id, sequence_no) do update set
          plan_item_id = excluded.plan_item_id,
          item_type = excluded.item_type,
          title = excluded.title,
          description = excluded.description,
          scheduled_at = excluded.scheduled_at,
          ends_at = excluded.ends_at,
          status = excluded.status
      """.update.run
      _ <- planItem.options.zipWithIndex.traverse_ { case (option, optionIndex) =>
        upsertPlanOption(group.groupId, planItem.sequenceNo, option, optionIndex)
      }
    yield ()

  private def upsertPlanOption(groupId: String, sequenceNo: Int, option: PlanOptionSeed, optionIndex: Int): ConnectionIO[Int] =
    val planItemIdValue = planItemId(groupId, sequenceNo)
    sql"""
      insert into group_plan_options(
        option_id, plan_item_id, resource_type, resource_id, resource_variant_code, resource_context, label, description, default_quantity, status
      ) values (
        ${optionId(planItemIdValue, optionIndex)},
        $planItemIdValue,
        ${option.resourceType},
        ${option.resourceId},
        ${option.resourceVariantCode},
        ${option.resourceContext},
        ${option.label},
        ${option.description},
        ${option.defaultQuantity},
        ${"Active"}
      )
      on conflict (option_id) do update set
        plan_item_id = excluded.plan_item_id,
        resource_type = excluded.resource_type,
        resource_id = excluded.resource_id,
        resource_variant_code = excluded.resource_variant_code,
        resource_context = excluded.resource_context,
        label = excluded.label,
        description = excluded.description,
        default_quantity = excluded.default_quantity,
        status = excluded.status
    """.update.run

  private def groupTitle(group: GroupSeed): String =
    f"${group.template.titlePrefix} ${group.groupIndex + 1}%03d"

  private def groupDescription(group: GroupSeed): String =
    s"${group.template.descriptionPrefix}。${group.template.homeCity.cityName} 出发，${group.template.city1.cityName} 与 ${group.template.city2.cityName} 之间安排了航班、酒店、高铁和景点，出发日期 ${group.startDate}，返回日期 ${group.endDate}。"

  private def groupDestination(group: GroupSeed): String =
    s"${group.template.city1.cityName} / ${group.template.city2.cityName}"

  private def groupTags(group: GroupSeed): List[String] =
    List(
      group.template.homeCity.cityName,
      group.template.city1.cityName,
      group.template.city2.cityName,
      "航班",
      "酒店",
      "火车",
      "景点"
    )

  private def roomTypeId(hotelId: String, suffix: String): String =
    s"roomtype-${hotelId.stripPrefix("hotel-")}-$suffix"

  private def attractionId(city: CityResource, suffix: String): String =
    s"demo-attraction-${city.citySlug}-$suffix"

  private def ticketTypeId(attractionId: String, suffix: String): String =
    s"ticket-type-$attractionId-$suffix"

  private def attractionNameForSuffix(suffix: String): String =
    suffix match
      case "museum"     => "城市博物馆"
      case "garden"     => "城市花园"
      case "theme-park" => "主题乐园"
      case "ocean-park" => "海洋乐园"
      case "night-tour" => "夜游灯会"
      case "science"    => "科技探索馆"
      case "art"        => "艺术馆"
      case "ancient"    => "古城景区"
      case other        => other

  private def flightDemoId(originCity: String, destinationCity: String, serviceDate: LocalDate, windowHour: Int, ordinal: Int): String =
    val routeKey = s"$originCity|$destinationCity|$serviceDate|$windowHour|$ordinal"
    s"flight-demo-${md5Hex(routeKey).take(24)}"

  private def md5Hex(value: String): String =
    HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)))

  private def localInstant(date: LocalDate, hour: Int, minute: Int): Instant =
    date.atTime(hour, minute).atZone(ZoneOffset.ofHours(8)).toInstant

  private def membershipId(groupId: String, userId: String): String =
    s"membership-$groupId-$userId"

  private def membershipTravelerId(groupId: String, userId: String): String =
    s"membership-traveler-$groupId-$userId"

  private def planItemId(groupId: String, sequenceNo: Int): String =
    f"plan-item-$groupId-$sequenceNo%02d"

  private def optionId(planItemId: String, optionIndex: Int): String =
    f"plan-option-$planItemId-${optionIndex + 1}%02d"

  private def normalizeDirectMemberA(userA: String, userB: String): String =
    if userA <= userB then userA else userB

  private def normalizeDirectMemberB(userA: String, userB: String): String =
    if userA <= userB then userB else userA

  private def directConversationId(groupId: String, userA: String, userB: String): String =
    s"conversation-direct-${md5Hex(s"$groupId|${normalizeDirectMemberA(userA, userB)}|${normalizeDirectMemberB(userA, userB)}").take(24)}"

  private def directParticipantId(groupId: String, userA: String, userB: String, participantUserId: String): String =
    s"participant-${md5Hex(s"$groupId|${normalizeDirectMemberA(userA, userB)}|${normalizeDirectMemberB(userA, userB)}|$participantUserId").take(24)}"

  private def publicParticipantId(conversationId: String, userId: String): String =
    s"participant-${md5Hex(s"$conversationId|$userId").take(24)}"

  private def cityResource(cityName: String): CityResource =
    cityResources.find(_.cityName == cityName).getOrElse(throw new IllegalArgumentException(s"Unknown city resource: $cityName"))
