// TourGroupReferenceDataSeederSetup handles setup and cleanup for demo groups.
package com.typesafe.travel.persistence

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.shared.kernel.EmailAddress
import doobie.*
import doobie.implicits.*

import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate}

object TourGroupReferenceDataSeederSetup:
  import TourGroupReferenceDataSeederData.*
  import TourGroupReferenceDataSeederSeeds.*
  import TourGroupReferenceDataSeederWrites.*
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    for
      _ <- seedHzhDemoUser(transactor)
      _ <- seedDemoUsers(transactor)
      _ <- ensureTourGroupColumns(transactor)
      _ <- ensureBlacklistsTable(transactor)
      demoGroupCount <- sql"select count(*) from tour_groups where group_id like 'tour-group-demo-%'".query[Long].unique.transact(transactor)
      _ <- if demoGroupCount >= ExpectedDemoGroupCount then IO.unit else seedDemoData(transactor)
    yield ()

  def seedDemoData(transactor: Transactor[IO]): IO[Unit] =
    for
      _ <- seedHzhDemoUser(transactor)
      _ <- cleanupDemoData(transactor)
      seededUsers <- seedDemoUsers(transactor)
      _ <- seedGroups(transactor, seededUsers)
      _ <- backfillGroupPlanItems(transactor, seededUsers)
    yield ()

  def seedHzhDemoUser(transactor: Transactor[IO]): IO[Unit] =
    upsertDemoUser(HzhDemoUserSeed, HzhDemoPasswordHash).transact(transactor).void

  def seedDemoUsers(transactor: Transactor[IO]): IO[Vector[SeededDemoUser]] =
    val userSeeds = buildUserSeeds()
    for
      hashedUsers <- userSeeds.traverse { seed =>
        hashPasswordForLoginEmail(DemoPassword, EmailAddress.unsafe(seed.email)).map(passwordHash => seed -> passwordHash)
      }
      seededUsers <- hashedUsers.traverse { case (seed, passwordHash) => upsertDemoUser(seed, passwordHash) }.transact(transactor)
    yield seededUsers

  def ensureTourGroupColumns(transactor: Transactor[IO]): IO[Unit] =
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

  def ensureBlacklistsTable(transactor: Transactor[IO]): IO[Unit] =
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

  def cleanupDemoData(transactor: Transactor[IO]): IO[Unit] =
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
