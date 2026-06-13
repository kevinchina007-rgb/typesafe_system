// TourGroupReferenceDataSeederWrites handles the actual insert and upsert statements.
package com.typesafe.travel.persistence

import cats.syntax.all.*
import doobie.*
import doobie.implicits.*

import java.security.MessageDigest
import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate, ZoneOffset}
import java.util.HexFormat

object TourGroupReferenceDataSeederWrites:
  import TourGroupReferenceDataSeederData.*
  def upsertDemoUser(seed: DemoUserSeed, passwordHash: String): ConnectionIO[SeededDemoUser] =
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

  def upsertTourGroup(group: GroupSeed): ConnectionIO[Int] =
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

  def upsertMembership(group: GroupSeed, member: SeededDemoUser, index: Int): ConnectionIO[Int] =
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

  def upsertMembershipTraveler(group: GroupSeed, member: SeededDemoUser, index: Int): ConnectionIO[Int] =
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

  def upsertChatSettings(group: GroupSeed): ConnectionIO[Int] =
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

  def upsertConversation(
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

  def upsertConversationParticipant(
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

  def upsertMessage(
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

  def upsertPlanItem(group: GroupSeed)(planItem: PlanItemSeed): ConnectionIO[Unit] =
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

  def upsertPlanOption(groupId: String, sequenceNo: Int, option: PlanOptionSeed, optionIndex: Int): ConnectionIO[Int] =
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

  def groupTitle(group: GroupSeed): String =
    f"${group.template.titlePrefix} ${group.groupIndex + 1}%03d"

  def groupDescription(group: GroupSeed): String =
    s"${group.template.descriptionPrefix}。${group.template.homeCity.cityName} 出发，${group.template.city1.cityName} 与 ${group.template.city2.cityName} 之间安排了航班、酒店、高铁和景点，出发日期 ${group.startDate}，返回日期 ${group.endDate}。"

  def groupDestination(group: GroupSeed): String =
    s"${group.template.city1.cityName} / ${group.template.city2.cityName}"

  def groupTags(group: GroupSeed): List[String] =
    List(
      group.template.homeCity.cityName,
      group.template.city1.cityName,
      group.template.city2.cityName,
      "航班",
      "酒店",
      "火车",
      "景点"
    )

  def roomTypeId(hotelId: String, suffix: String): String =
    s"roomtype-${hotelId.stripPrefix("hotel-")}-$suffix"

  def attractionId(city: CityResource, suffix: String): String =
    s"demo-attraction-${city.citySlug}-$suffix"

  def ticketTypeId(attractionId: String, suffix: String): String =
    s"ticket-type-$attractionId-$suffix"

  def attractionNameForSuffix(suffix: String): String =
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

  def flightDemoId(originCity: String, destinationCity: String, serviceDate: LocalDate, windowHour: Int, ordinal: Int): String =
    val routeKey = s"$originCity|$destinationCity|$serviceDate|$windowHour|$ordinal"
    s"flight-demo-${md5Hex(routeKey).take(24)}"

  def md5Hex(value: String): String =
    HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)))

  def localInstant(date: LocalDate, hour: Int, minute: Int): Instant =
    date.atTime(hour, minute).atZone(ZoneOffset.ofHours(8)).toInstant

  def membershipId(groupId: String, userId: String): String =
    s"membership-$groupId-$userId"

  def membershipTravelerId(groupId: String, userId: String): String =
    s"membership-traveler-$groupId-$userId"

  def planItemId(groupId: String, sequenceNo: Int): String =
    f"plan-item-$groupId-$sequenceNo%02d"

  def optionId(planItemId: String, optionIndex: Int): String =
    f"plan-option-$planItemId-${optionIndex + 1}%02d"

  def normalizeDirectMemberA(userA: String, userB: String): String =
    if userA <= userB then userA else userB

  def normalizeDirectMemberB(userA: String, userB: String): String =
    if userA <= userB then userB else userA

  def directConversationId(groupId: String, userA: String, userB: String): String =
    s"conversation-direct-${md5Hex(s"$groupId|${normalizeDirectMemberA(userA, userB)}|${normalizeDirectMemberB(userA, userB)}").take(24)}"

  def directParticipantId(groupId: String, userA: String, userB: String, participantUserId: String): String =
    s"participant-${md5Hex(s"$groupId|${normalizeDirectMemberA(userA, userB)}|${normalizeDirectMemberB(userA, userB)}|$participantUserId").take(24)}"

  def publicParticipantId(conversationId: String, userId: String): String =
    s"participant-${md5Hex(s"$conversationId|$userId").take(24)}"

  def cityResource(cityName: String): CityResource =
    cityResources.find(_.cityName == cityName).getOrElse(throw new IllegalArgumentException(s"Unknown city resource: $cityName"))
