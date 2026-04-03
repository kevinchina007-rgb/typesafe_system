package com.typesafe.travel.api.memory

import cats.effect.kernel.Sync
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.shared.kernel.*

import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap

final class InMemoryAttractionRepository[F[_]: Sync] private (
    attractionState: TrieMap[AttractionId, Attraction],
    attractionSequence: AtomicLong,
    ticketTypeSequence: AtomicLong,
    ruleSequence: AtomicLong,
    sessionSequence: AtomicLong
) extends AttractionRepository[F]:
  override def nextAttractionId: F[AttractionId] =
    Sync[F].delay(AttractionId(s"attraction-${attractionSequence.incrementAndGet()}"))

  override def nextTicketTypeId: F[TicketTypeId] =
    Sync[F].delay(TicketTypeId(s"ticket-type-${ticketTypeSequence.incrementAndGet()}"))

  override def nextTicketEligibilityRuleId: F[TicketEligibilityRuleId] =
    Sync[F].delay(TicketEligibilityRuleId(s"ticket-rule-${ruleSequence.incrementAndGet()}"))

  override def nextAttractionTicketSessionId: F[AttractionTicketSessionId] =
    Sync[F].delay(AttractionTicketSessionId(s"ticket-session-${sessionSequence.incrementAndGet()}"))

  override def findAttractionById(attractionId: AttractionId): F[Option[Attraction]] =
    Sync[F].delay(attractionState.get(attractionId))

  override def findAttractionsByManagerId(managerId: ManagerId): F[List[Attraction]] =
    Sync[F].delay(attractionState.values.filter(_.managerId == managerId).toList.sortBy(_.createdAt))

  override def listPublishedAttractions: F[List[Attraction]] =
    Sync[F].delay(attractionState.values.filter(_.attractionStatus == AttractionStatus.Published).toList.sortBy(_.createdAt))

  override def saveAttraction(attraction: Attraction): F[Attraction] =
    Sync[F].delay {
      attractionState.put(attraction.attractionId, attraction)
      attraction
    }

object InMemoryAttractionRepository:
  def create[F[_]: Sync]: InMemoryAttractionRepository[F] =
    new InMemoryAttractionRepository[F](TrieMap.empty, AtomicLong(100), AtomicLong(1000), AtomicLong(1000), AtomicLong(1000))
