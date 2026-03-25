package com.typesafe.travel.api.memory

import cats.effect.kernel.Sync
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*

import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap

final class InMemoryTravelerProfileRepository[F[_]: Sync] private (
    travelerState: TrieMap[TravelerId, TravelerProfile],
    travelerSequence: AtomicLong
) extends TravelerProfileRepository[F]:
  override def nextTravelerId: F[TravelerId] =
    Sync[F].delay(TravelerId(s"traveler-${travelerSequence.incrementAndGet()}"))

  override def findTravelerProfileById(travelerId: TravelerId): F[Option[TravelerProfile]] =
    Sync[F].delay(travelerState.get(travelerId))

  override def findTravelerProfilesByOwnerUserId(ownerUserId: UserId): F[List[TravelerProfile]] =
    Sync[F].delay(travelerState.values.filter(_.ownerUserId == ownerUserId).toList.sortBy(_.travelerId.value))

  override def saveTravelerProfile(travelerProfile: TravelerProfile): F[TravelerProfile] =
    Sync[F].delay {
      travelerState.put(travelerProfile.travelerId, travelerProfile)
      travelerProfile
    }

object InMemoryTravelerProfileRepository:
  def create[F[_]: Sync]: InMemoryTravelerProfileRepository[F] =
    new InMemoryTravelerProfileRepository[F](TrieMap.empty, AtomicLong(0))
