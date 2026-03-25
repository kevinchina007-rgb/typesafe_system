package com.typesafe.travel.identity.domain

import com.typesafe.travel.shared.kernel.*

trait UserRepository[F[_]]:
  def nextUserId: F[UserId]
  def findByUserId(userId: UserId): F[Option[User]]
  def findByPrimaryEmailAddress(primaryEmailAddress: EmailAddress): F[Option[User]]
  def saveUser(user: User): F[User]
