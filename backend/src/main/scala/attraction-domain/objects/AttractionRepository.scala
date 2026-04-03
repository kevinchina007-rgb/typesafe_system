package com.typesafe.travel.attraction.domain

import com.typesafe.travel.shared.kernel.*

trait AttractionRepository[F[_]]:
  def nextAttractionId: F[AttractionId]
  def nextTicketTypeId: F[TicketTypeId]
  def nextTicketEligibilityRuleId: F[TicketEligibilityRuleId]
  def nextAttractionTicketSessionId: F[AttractionTicketSessionId]
  def findAttractionById(attractionId: AttractionId): F[Option[Attraction]]
  def findAttractionsByManagerId(managerId: ManagerId): F[List[Attraction]]
  def listPublishedAttractions: F[List[Attraction]]
  def saveAttraction(attraction: Attraction): F[Attraction]
