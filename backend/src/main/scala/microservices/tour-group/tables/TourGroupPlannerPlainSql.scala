package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.{Instant, LocalDate}
import java.util.UUID

object TourGroupPlannerPlainSql:
  def create(connection: Connection, input: CreateTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val groupId = nextId("group")
      val membershipId = nextId("membership")
      PlainSqlSupport.withStatement(
        connection,
        "insert into tour_groups(group_id, organizer_user_id, title, description, destination, start_date, end_date, capacity, status, created_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, groupId)
        statement.setString(2, input.organizerUserId)
        statement.setString(3, input.title.trim)
        statement.setString(4, input.description.trim)
        statement.setString(5, input.destination.trim)
        statement.setDate(6, java.sql.Date.valueOf(LocalDate.parse(input.startDate)))
        statement.setDate(7, java.sql.Date.valueOf(LocalDate.parse(input.endDate)))
        statement.setInt(8, input.capacity)
        statement.setString(9, "Open")
        statement.setTimestamp(10, Timestamp.from(now))
        statement.executeUpdate()
      }
      insertMembership(connection, membershipId, groupId, input.organizerUserId, now)
      details(connection, groupId)
    }

  def list(connection: Connection): IO[TourGroupListPlannerResponse] =
    IO.blocking {
      TourGroupListPlannerResponse(
        PlainSqlSupport.withStatement(
          connection,
          """
            select g.group_id, g.organizer_user_id, g.title, g.description, g.destination, g.start_date, g.end_date, g.capacity, g.status, g.created_at,
              (select count(*) from tour_group_memberships m where m.group_id = g.group_id and m.status = 'Active') as member_count,
              (select count(*) from tour_group_membership_travelers mt inner join tour_group_memberships m on m.membership_id = mt.membership_id where m.group_id = g.group_id and mt.status = 'Active') as active_traveler_count,
              (select count(*) from group_plan_selections s where s.group_id = g.group_id and s.status = 'Submitted') as pending_selection_count,
              (select count(*) from group_plan_selections s where s.group_id = g.group_id and s.status = 'OrganizerConfirmed') as confirmed_selection_count,
              (select count(*) from group_selection_order_links l inner join group_plan_selections s on s.selection_id = l.selection_id where s.group_id = g.group_id) as converted_order_count
            from tour_groups g
            order by g.created_at desc, g.group_id
          """
        ) { statement =>
          PlainSqlSupport.queryList(statement)(readSummary)
        }
      )
    }

  def get(connection: Connection, input: TourGroupByIdPlannerRequest): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking(details(connection, input.groupId))

  def join(connection: Connection, input: JoinTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val existingMembershipId =
        PlainSqlSupport.withStatement(connection, "select membership_id from tour_group_memberships where group_id = ? and user_id = ? and status = 'Active' fetch first 1 row only") { statement =>
          statement.setString(1, input.groupId)
          statement.setString(2, input.userId)
          val resultSet = statement.executeQuery()
          try if resultSet.next() then Some(resultSet.getString("membership_id")) else None
          finally resultSet.close()
        }
      existingMembershipId.getOrElse(insertMembership(connection, nextId("membership"), input.groupId, input.userId, now))
      details(connection, input.groupId)
    }

  def addMembershipTraveler(connection: Connection, input: AddMembershipTravelerPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val membershipId = activeMembershipId(connection, input.groupId, input.userId)
      PlainSqlSupport.withStatement(
        connection,
        "insert into tour_group_membership_travelers(membership_traveler_id, membership_id, traveler_id, joined_at, status) values (?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, nextId("membership-traveler"))
        statement.setString(2, membershipId)
        statement.setString(3, input.travelerId)
        statement.setTimestamp(4, Timestamp.from(now))
        statement.setString(5, "Active")
        statement.executeUpdate()
      }
      details(connection, input.groupId)
    }

  private def details(connection: Connection, groupId: String): TourGroupDetailsPlannerResponse =
    val group = groupSummary(connection, groupId)
    TourGroupDetailsPlannerResponse(
      group = group,
      memberships = memberships(connection, groupId),
      membershipTravelers = membershipTravelers(connection, groupId)
    )

  private def groupSummary(connection: Connection, groupId: String): TourGroupSummaryPlannerResponse =
    PlainSqlSupport.withStatement(
      connection,
      """
        select g.group_id, g.organizer_user_id, g.title, g.description, g.destination, g.start_date, g.end_date, g.capacity, g.status, g.created_at,
          (select count(*) from tour_group_memberships m where m.group_id = g.group_id and m.status = 'Active') as member_count,
          (select count(*) from tour_group_membership_travelers mt inner join tour_group_memberships m on m.membership_id = mt.membership_id where m.group_id = g.group_id and mt.status = 'Active') as active_traveler_count,
          (select count(*) from group_plan_selections s where s.group_id = g.group_id and s.status = 'Submitted') as pending_selection_count,
          (select count(*) from group_plan_selections s where s.group_id = g.group_id and s.status = 'OrganizerConfirmed') as confirmed_selection_count,
          (select count(*) from group_selection_order_links l inner join group_plan_selections s on s.selection_id = l.selection_id where s.group_id = g.group_id) as converted_order_count
        from tour_groups g
        where g.group_id = ?
      """
    ) { statement =>
      statement.setString(1, groupId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readSummary(resultSet) else throw new IllegalArgumentException(s"Tour group '$groupId' was not found")
      finally resultSet.close()
    }

  private def readSummary(row: ResultSet): TourGroupSummaryPlannerResponse =
    val capacity = row.getInt("capacity")
    val activeTravelerCount = row.getInt("active_traveler_count")
    TourGroupSummaryPlannerResponse(
      groupId = row.getString("group_id"),
      organizerUserId = row.getString("organizer_user_id"),
      title = row.getString("title"),
      description = row.getString("description"),
      destination = row.getString("destination"),
      startDate = row.getDate("start_date").toLocalDate.toString,
      endDate = row.getDate("end_date").toLocalDate.toString,
      capacity = capacity,
      usedCapacity = activeTravelerCount,
      isFull = activeTravelerCount >= capacity,
      memberCount = row.getInt("member_count"),
      activeTravelerCount = activeTravelerCount,
      pendingSelectionCount = row.getInt("pending_selection_count"),
      confirmedSelectionCount = row.getInt("confirmed_selection_count"),
      convertedOrderCount = row.getInt("converted_order_count"),
      status = row.getString("status"),
      createdAt = row.getTimestamp("created_at").toInstant.toString
    )

  private def memberships(connection: Connection, groupId: String): List[TourGroupMembershipPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select membership_id, user_id, joined_at, status from tour_group_memberships where group_id = ? order by joined_at, membership_id") { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        val userId = row.getString("user_id")
        TourGroupMembershipPlannerResponse(row.getString("membership_id"), userId, userId, row.getString("status"), row.getTimestamp("joined_at").toInstant.toString)
      }
    }

  private def membershipTravelers(connection: Connection, groupId: String): List[TourGroupMembershipTravelerPlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select mt.membership_traveler_id, mt.membership_id, mt.traveler_id, mt.joined_at, mt.status
        from tour_group_membership_travelers mt
        inner join tour_group_memberships m on m.membership_id = mt.membership_id
        where m.group_id = ?
        order by mt.joined_at, mt.membership_traveler_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        TourGroupMembershipTravelerPlannerResponse(row.getString("membership_traveler_id"), row.getString("membership_id"), row.getString("traveler_id"), row.getString("status"), row.getTimestamp("joined_at").toInstant.toString)
      }
    }

  private def insertMembership(connection: Connection, membershipId: String, groupId: String, userId: String, joinedAt: Instant): String =
    PlainSqlSupport.withStatement(connection, "insert into tour_group_memberships(membership_id, group_id, user_id, joined_at, status) values (?, ?, ?, ?, ?)") { statement =>
      statement.setString(1, membershipId)
      statement.setString(2, groupId)
      statement.setString(3, userId)
      statement.setTimestamp(4, Timestamp.from(joinedAt))
      statement.setString(5, "Active")
      statement.executeUpdate()
    }
    membershipId

  private def activeMembershipId(connection: Connection, groupId: String, userId: String): String =
    PlainSqlSupport.withStatement(connection, "select membership_id from tour_group_memberships where group_id = ? and user_id = ? and status = 'Active' fetch first 1 row only") { statement =>
      statement.setString(1, groupId)
      statement.setString(2, userId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getString("membership_id") else throw new IllegalArgumentException("Active tour group membership was not found")
      finally resultSet.close()
    }

  private def nextId(prefix: String): String =
    s"$prefix-${UUID.randomUUID().toString.take(12)}"
