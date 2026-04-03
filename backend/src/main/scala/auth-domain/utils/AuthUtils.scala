package com.typesafe.travel.auth.domain

import cats.effect.kernel.Sync
import com.typesafe.travel.shared.kernel.*

import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

def createUserCredential(
    credentialId: CredentialId,
    userId: UserId,
    loginEmail: EmailAddress,
    passwordHash: String,
    createdAt: Instant
): Either[AuthError, UserCredential] =
  if passwordHash.trim.isEmpty then Left(AuthError.PasswordWasEmpty)
  else Right(UserCredential(credentialId, userId, loginEmail, passwordHash, CredentialStatus.Active, createdAt, createdAt, createdAt))

def restorePersistedUserCredential(
    credentialId: CredentialId,
    userId: UserId,
    loginEmail: EmailAddress,
    passwordHash: String,
    status: CredentialStatus,
    createdAt: Instant,
    updatedAt: Instant,
    passwordUpdatedAt: Instant
): UserCredential =
  UserCredential(credentialId, userId, loginEmail, passwordHash, status, createdAt, updatedAt, passwordUpdatedAt)

def createManagerCredential(
    credentialId: CredentialId,
    managerType: AuthManagerType,
    managerId: ManagerId,
    loginEmail: EmailAddress,
    passwordHash: String,
    createdAt: Instant
): Either[AuthError, ManagerCredential] =
  if passwordHash.trim.isEmpty then Left(AuthError.PasswordWasEmpty)
  else Right(ManagerCredential(credentialId, managerType, managerId, loginEmail, passwordHash, CredentialStatus.Active, createdAt, createdAt, createdAt))

def restorePersistedManagerCredential(
    credentialId: CredentialId,
    managerType: AuthManagerType,
    managerId: ManagerId,
    loginEmail: EmailAddress,
    passwordHash: String,
    status: CredentialStatus,
    createdAt: Instant,
    updatedAt: Instant,
    passwordUpdatedAt: Instant
): ManagerCredential =
  ManagerCredential(credentialId, managerType, managerId, loginEmail, passwordHash, status, createdAt, updatedAt, passwordUpdatedAt)

def createAuthSession(sessionId: SessionId, principal: CurrentPrincipal, createdAt: Instant, expiresAt: Instant): AuthSession =
  principal match
    case CurrentUserPrincipal(userId) =>
      AuthSession(sessionId, AuthActorType.User, userId.value, None, createdAt, createdAt, expiresAt, AuthSessionStatus.Active)
    case CurrentManagerPrincipal(managerType, managerId) =>
      AuthSession(sessionId, AuthActorType.Manager, managerId.value, Some(managerType), createdAt, createdAt, expiresAt, AuthSessionStatus.Active)

def restorePersistedSession(
    sessionId: SessionId,
    actorType: AuthActorType,
    actorId: String,
    managerType: Option[AuthManagerType],
    createdAt: Instant,
    lastSeenAt: Instant,
    expiresAt: Instant,
    status: AuthSessionStatus
): AuthSession =
  AuthSession(sessionId, actorType, actorId, managerType, createdAt, lastSeenAt, expiresAt, status)

def toCurrentPrincipal(authSession: AuthSession): Either[AuthError, CurrentPrincipal] =
  authSession.actorType match
    case AuthActorType.User => Right(CurrentUserPrincipal(UserId(authSession.actorId)))
    case AuthActorType.Manager =>
      authSession.managerType.map(managerType => CurrentManagerPrincipal(managerType, ManagerId(authSession.actorId))).toRight(AuthError.ManagerSessionWasRequired)

def hashPassword[F[_]: Sync](rawPassword: String): F[String] =
  Sync[F].delay {
    val normalizedPassword = rawPassword.trim
    validatePasswordStrength(normalizedPassword, None)
    val random = SecureRandom()
    val saltBytes = Array.ofDim[Byte](16)
    random.nextBytes(saltBytes)
    val iterations = 210000
    val hashBytes = pbkdf2(normalizedPassword, saltBytes, iterations)
    val encoder = Base64.getEncoder
    "pbkdf2-sha256$" + iterations + "$" + encoder.encodeToString(saltBytes) + "$" + encoder.encodeToString(hashBytes)
  }

def hashPasswordForLoginEmail[F[_]: Sync](rawPassword: String, loginEmail: EmailAddress): F[String] =
  Sync[F].delay {
    val normalizedPassword = rawPassword.trim
    validatePasswordStrength(normalizedPassword, Some(loginEmail))
    val random = SecureRandom()
    val saltBytes = Array.ofDim[Byte](16)
    random.nextBytes(saltBytes)
    val iterations = 210000
    val hashBytes = pbkdf2(normalizedPassword, saltBytes, iterations)
    val encoder = Base64.getEncoder
    "pbkdf2-sha256$" + iterations + "$" + encoder.encodeToString(saltBytes) + "$" + encoder.encodeToString(hashBytes)
  }

def verifyPassword[F[_]: Sync](rawPassword: String, passwordHash: String): F[Boolean] =
  Sync[F].delay {
    val segments = passwordHash.split("\\$")
    if segments.length != 4 || segments(0) != "pbkdf2-sha256" then false
    else
      val iterations = segments(1).toInt
      val decoder = Base64.getDecoder
      val saltBytes = decoder.decode(segments(2))
      val expectedHashBytes = decoder.decode(segments(3))
      val actualHashBytes = pbkdf2(rawPassword.trim, saltBytes, iterations)
      java.security.MessageDigest.isEqual(expectedHashBytes, actualHashBytes)
  }

private def pbkdf2(rawPassword: String, saltBytes: Array[Byte], iterations: Int): Array[Byte] =
  val keySpec = PBEKeySpec(rawPassword.toCharArray, saltBytes, iterations, 256)
  try SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(keySpec).getEncoded
  finally keySpec.clearPassword()

private def validatePasswordStrength(rawPassword: String, loginEmail: Option[EmailAddress]): Unit =
  val normalizedPassword = rawPassword.trim
  if normalizedPassword.isEmpty then throw AuthError.PasswordWasEmpty
  if normalizedPassword.length < 10 then throw AuthError.PasswordWasTooShort

  val hasLetter = normalizedPassword.exists(_.isLetter)
  val hasDigit = normalizedPassword.exists(_.isDigit)
  val lowercasePassword = normalizedPassword.toLowerCase
  val forbiddenValues = Set("password", "password123", "1234567890", "qwerty123", "admin123456", "hzhishengheng")
  val emailLocalPart = loginEmail.map(_.value.takeWhile(_ != '@').toLowerCase)

  val isWeak =
    !hasLetter ||
      !hasDigit ||
      forbiddenValues.contains(lowercasePassword) ||
      emailLocalPart.contains(lowercasePassword) ||
      normalizedPassword.distinct.length <= 3

  if isWeak then throw AuthError.PasswordWasTooWeak
