package com.typesafe.travel.traveler.domain

import com.typesafe.travel.shared.kernel.*

trait TravelerProfileRepository[F[_]]:
  def nextId: F[TravelerId]
  def findById(id: TravelerId): F[Option[TravelerProfile]]
  def findByOwner(ownerUserId: UserId): F[List[TravelerProfile]]
  def save(profile: TravelerProfile): F[TravelerProfile]
