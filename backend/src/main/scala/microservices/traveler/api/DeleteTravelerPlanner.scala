// DeleteTravelerPlanner 是旅客模块的删除入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.traveler.domain

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection

object DeleteTravelerPlanner extends ConnectionApiPlan[DeleteTravelerPlannerRequest, TravelerDeletedPlannerResponse]:
  override val name: String = "DeleteTravelerPlanner"

  override def plan(input: DeleteTravelerPlannerRequest, connection: Connection): IO[TravelerDeletedPlannerResponse] =
    for
      ownerUserId <- requireActor(input.actingUserId, input.ownerUserId)
      travelerId = TravelerId(input.travelerId)
      traveler <- TravelerPlannerPlainSql.findById(connection, travelerId).flatMap(_.liftTo[IO](TravelerError.TravelerProfileWasNotFound(travelerId)))
      _ <- ensureTravelerProfileOwnedBy(traveler, ownerUserId).liftTo[IO]
      existingProfiles <- TravelerPlannerPlainSql.listByOwner(connection, ownerUserId)
      visibleRemainingProfiles = existingProfiles.filter(profile =>
        profile.travelerId != travelerId && profile.travelerProfileStatus != TravelerProfileStatus.Archived
      )
      _ <- if traveler.isDefaultTravelerProfile then
        visibleRemainingProfiles match
          case nextDefault :: rest =>
            for
              promoted <- markTravelerProfileAsDefault(nextDefault).liftTo[IO]
              _ <- TravelerPlannerPlainSql.save(connection, promoted)
              _ <- rest.traverse(profile => TravelerPlannerPlainSql.save(connection, clearTravelerProfileDefault(profile))).void
              _ <- TravelerPlannerPlainSql.updateUserDefaultTraveler(connection, ownerUserId, Some(promoted.travelerId))
            yield ()
          case Nil =>
            TravelerPlannerPlainSql.updateUserDefaultTraveler(connection, ownerUserId, None)
      else IO.unit
      archived <- archiveTravelerProfile(clearTravelerProfileDefault(traveler)).liftTo[IO]
      _ <- TravelerPlannerPlainSql.save(connection, archived)
    yield TravelerDeletedPlannerResponse(deleted = true, hidden = true)
