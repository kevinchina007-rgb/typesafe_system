package com.typesafe.travel.persistence.identity

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import doobie.*
import doobie.implicits.*

import java.time.Instant
import java.util.UUID

final class DoobieUserRepository[F[_]: Async](
    transactor: Transactor[F]
) extends UserRepository[F]:
  override def nextUserId: F[UserId] =
    Sync[F].delay(UserId(s"user-${UUID.randomUUID().toString.take(12)}"))

  override def findByUserId(userId: UserId): F[Option[User]] =
    sql"""
      select
        user_id,
        email,
        phone,
        nickname,
        avatar_url,
        membership_level,
        points,
        status,
        default_traveler_id,
        created_at
      from users
      where user_id = ${userId.value}
    """
      .query[(String, String, String, String, Option[String], String, Long, String, Option[String], Instant)]
      .option
      .transact(transactor)
      .flatMap(_.traverse(buildUser))

  override def findByPrimaryEmailAddress(primaryEmailAddress: EmailAddress): F[Option[User]] =
    sql"""
      select
        user_id,
        email,
        phone,
        nickname,
        avatar_url,
        membership_level,
        points,
        status,
        default_traveler_id,
        created_at
      from users
      where email = ${primaryEmailAddress.value}
    """
      .query[(String, String, String, String, Option[String], String, Long, String, Option[String], Instant)]
      .option
      .transact(transactor)
      .flatMap(_.traverse(buildUser))

  override def saveUser(user: User): F[User] =
    val updateExistingUser =
      sql"""
        update users
        set
          email = ${user.primaryEmailAddress.value},
          phone = ${user.userPhoneNumber.value},
          nickname = ${user.userDisplayName.value},
          avatar_url = ${user.avatarUrl.map(_.value)},
          membership_level = ${user.membershipLevel.toString},
          points = ${user.loyaltyPoints.value},
          status = ${user.userAccountStatus.toString},
          default_traveler_id = ${user.defaultTravelerProfileId.map(_.value)},
          created_at = ${user.registeredAt}
        where user_id = ${user.userId.value}
      """.update.run

    val insertNewUser =
      sql"""
        insert into users (
          user_id, email, phone, nickname, avatar_url, membership_level, points, status, default_traveler_id, created_at
        ) values (
          ${user.userId.value},
          ${user.primaryEmailAddress.value},
          ${user.userPhoneNumber.value},
          ${user.userDisplayName.value},
          ${user.avatarUrl.map(_.value)},
          ${user.membershipLevel.toString},
          ${user.loyaltyPoints.value},
          ${user.userAccountStatus.toString},
          ${user.defaultTravelerProfileId.map(_.value)},
          ${user.registeredAt}
        )
      """.update.run

    updateExistingUser.transact(transactor).flatMap { updatedRowCount =>
      if updatedRowCount > 0 then Async[F].pure(user)
      else insertNewUser.transact(transactor).as(user)
    }

  private def buildUser(
      row: (String, String, String, String, Option[String], String, Long, String, Option[String], Instant)
  ): F[User] =
    val (
      userIdValue,
      emailValue,
      phoneValue,
      nicknameValue,
      avatarUrlValue,
      membershipLevelValue,
      pointsValue,
      statusValue,
      defaultTravelerIdValue,
      createdAtValue
    ) = row

    for
      primaryEmailAddress <- Async[F].fromEither(EmailAddress.create(emailValue))
      userDisplayName <- Async[F].fromEither(PersonName.create(nicknameValue))
      userPhoneNumber <- Async[F].fromEither(ContactNumber.create(phoneValue))
      avatarUrl <- avatarUrlValue.traverse(avatarUrlText => Async[F].fromEither(AvatarUrl.create(avatarUrlText)))
      loyaltyPoints <- Async[F].fromEither(Points.create(pointsValue))
    yield com.typesafe.travel.identity.domain.restorePersistedUser(
      userId = UserId(userIdValue),
      primaryEmailAddress = primaryEmailAddress,
      userDisplayName = userDisplayName,
      userPhoneNumber = userPhoneNumber,
      avatarUrl = avatarUrl,
      userAccountStatus = UserAccountStatus.fromText(statusValue),
      membershipLevel = UserMembershipLevel.fromText(membershipLevelValue),
      loyaltyPoints = loyaltyPoints,
      defaultTravelerProfileId = defaultTravelerIdValue.map(TravelerId.apply),
      registeredAt = createdAtValue
    )
