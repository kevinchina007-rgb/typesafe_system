package com.typesafe.travel.identity.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import java.time.Instant

trait UserService[F[_]]:
  def registerUser(
      primaryEmailAddress: EmailAddress,
      userDisplayName: PersonName,
      userPhoneNumber: ContactNumber,
      registeredAt: Instant
  ): F[User]

  def loginUserByEmail(primaryEmailAddress: EmailAddress): F[User]

  def activateUser(userId: UserId): F[User]

  def suspendUser(userId: UserId): F[User]

  def accrueUserPoints(userId: UserId, additionalPoints: Points): F[User]

  def assignDefaultTraveler(userId: UserId, travelerId: TravelerId): F[User]

  def updateUserAvatar(userId: UserId, avatarUrl: AvatarUrl): F[User]

final class LiveUserService[F[_]: MonadThrow](
    userRepository: UserRepository[F]
) extends UserService[F]:

  override def registerUser(
      primaryEmailAddress: EmailAddress,
      userDisplayName: PersonName,
      userPhoneNumber: ContactNumber,
      registeredAt: Instant
  ): F[User] =
    userRepository
      .findByPrimaryEmailAddress(primaryEmailAddress)
      .flatMap {
        case Some(_) => MonadThrow[F].raiseError(UserError.UserEmailAddressAlreadyExists(primaryEmailAddress))
        case None =>
          userRepository.nextUserId.flatMap { generatedUserId =>
            userRepository.saveUser(
              User.registerNewUser(
                userId = generatedUserId,
                primaryEmailAddress = primaryEmailAddress,
                userDisplayName = userDisplayName,
                userPhoneNumber = userPhoneNumber,
                registeredAt = registeredAt
              )
            )
          }
      }

  override def loginUserByEmail(primaryEmailAddress: EmailAddress): F[User] =
    userRepository
      .findByPrimaryEmailAddress(primaryEmailAddress)
      .flatMap(_.liftTo[F](UserError.UserWasNotFoundByEmail(primaryEmailAddress)))

  override def activateUser(userId: UserId): F[User] =
    loadUser(userId)
      .flatMap(user => MonadThrow[F].fromEither(user.activateUserAccount))
      .flatMap(userRepository.saveUser)

  override def suspendUser(userId: UserId): F[User] =
    loadUser(userId)
      .flatMap(user => MonadThrow[F].fromEither(user.suspendUserAccount))
      .flatMap(userRepository.saveUser)

  override def accrueUserPoints(userId: UserId, additionalPoints: Points): F[User] =
    loadUser(userId)
      .flatMap(user => MonadThrow[F].fromEither(user.accrueUserLoyaltyPoints(additionalPoints)))
      .flatMap(userRepository.saveUser)

  override def assignDefaultTraveler(userId: UserId, travelerId: TravelerId): F[User] =
    loadUser(userId)
      .flatMap(user => MonadThrow[F].fromEither(user.assignDefaultTravelerProfile(travelerId)))
      .flatMap(userRepository.saveUser)

  override def updateUserAvatar(userId: UserId, avatarUrl: AvatarUrl): F[User] =
    loadUser(userId)
      .flatMap(user => MonadThrow[F].fromEither(user.updateAvatarUrl(avatarUrl)))
      .flatMap(userRepository.saveUser)

  private def loadUser(userId: UserId): F[User] =
    userRepository
      .findByUserId(userId)
      .flatMap(_.liftTo[F](UserError.UserWasNotFound(userId)))
