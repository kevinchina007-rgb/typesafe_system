package com.typesafe.travel.api.memory

import cats.effect.kernel.{Ref, Sync}
import cats.syntax.all.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.shared.kernel.*

final class InMemoryUserRepository[F[_]: Sync] private (
    userState: Ref[F, Map[UserId, User]],
    userSequence: Ref[F, Long]
) extends UserRepository[F]:
  override def nextUserId: F[UserId] =
    userSequence.modify { currentValue =>
      val nextValue = currentValue + 1
      nextValue -> UserId(s"user-$nextValue")
    }

  override def findByUserId(userId: UserId): F[Option[User]] =
    userState.get.map(_.get(userId))

  override def findByPrimaryEmailAddress(primaryEmailAddress: EmailAddress): F[Option[User]] =
    userState.get.map(_.values.find(_.primaryEmailAddress == primaryEmailAddress))

  override def saveUser(user: User): F[User] =
    userState.update(_ + (user.userId -> user)).as(user)

object InMemoryUserRepository:
  def create[F[_]: Sync]: InMemoryUserRepository[F] =
    new InMemoryUserRepository[F](
      userState = Ref.unsafe[F, Map[UserId, User]](Map.empty),
      userSequence = Ref.unsafe[F, Long](0L)
    )
