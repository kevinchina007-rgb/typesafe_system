package com.typesafe.travel.traveler.domain

import com.typesafe.travel.shared.kernel.*

trait TravelerProfileRepository[F[_]]:
  def nextTravelerId: F[TravelerId]
  def findTravelerProfileById(travelerId: TravelerId): F[Option[TravelerProfile]]
  def findTravelerProfilesByOwnerUserId(ownerUserId: UserId): F[List[TravelerProfile]]
  def saveTravelerProfile(travelerProfile: TravelerProfile): F[TravelerProfile]
