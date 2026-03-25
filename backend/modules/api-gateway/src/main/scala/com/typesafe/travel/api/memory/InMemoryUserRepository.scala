package com.typesafe.travel.api.memory

import cats.effect.kernel.Sync
import cats.syntax.all.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.shared.kernel.*

import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap

final class InMemoryUserRepository[F[_]: Sync] private (
    userState: TrieMap[UserId, User],
    userSequence: AtomicLong
) extends UserRepository[F]:
  override def nextUserId: F[UserId] =
    Sync[F].delay(UserId(s"user-${userSequence.incrementAndGet()}"))

  override def findByUserId(userId: UserId): F[Option[User]] =
    Sync[F].delay(userState.get(userId))

  override def findByPrimaryEmailAddress(primaryEmailAddress: EmailAddress): F[Option[User]] =
    Sync[F].delay(userState.values.find(_.primaryEmailAddress == primaryEmailAddress))

  override def saveUser(user: User): F[User] =
    Sync[F].delay {
      userState.put(user.userId, user)
      user
    }

object InMemoryUserRepository:
  def create[F[_]: Sync]: InMemoryUserRepository[F] =
    new InMemoryUserRepository[F](TrieMap.empty, AtomicLong(0))
