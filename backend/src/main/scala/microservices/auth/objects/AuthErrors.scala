// AuthErrors 瀹氫箟璁よ瘉妯″潡鐨勯敊璇ā鍨嬨€?
package com.typesafe.travel.auth.domain

import com.typesafe.travel.shared.kernel.*

sealed trait AuthError extends DomainError:
  def message: String

object AuthError:
  case object PasswordWasEmpty extends AuthError:
    override val message: String = "Password cannot be empty"

  case object PasswordWasTooShort extends AuthError:
    override val message: String = "Password must be at least 10 characters"

  case object PasswordWasTooWeak extends AuthError:
    override val message: String = "Password must include letters and numbers and avoid weak defaults"

  final case class UserCredentialWasNotFoundByEmail(loginEmail: EmailAddress) extends AuthError:
    override val message: String = s"User credential '${loginEmail.value}' was not found"

  final case class UserCredentialWasNotFoundByUserId(userId: UserId) extends AuthError:
    override val message: String = s"User credential for '${userId.value}' was not found"

  final case class ManagerCredentialWasNotFoundByEmail(managerType: AuthManagerType, loginEmail: EmailAddress) extends AuthError:
    override val message: String = s"$managerType credential '${loginEmail.value}' was not found"

  final case class ManagerCredentialWasNotFound(managerType: AuthManagerType, managerId: ManagerId) extends AuthError:
    override val message: String = s"$managerType credential for '${managerId.value}' was not found"

  final case class UserCredentialAlreadyExists(loginEmail: EmailAddress) extends AuthError:
    override val message: String = s"User credential '${loginEmail.value}' already exists"

  final case class ManagerCredentialAlreadyExists(managerType: AuthManagerType, loginEmail: EmailAddress) extends AuthError:
    override val message: String = s"$managerType credential '${loginEmail.value}' already exists"

  final case class CredentialWasDisabled(loginEmail: EmailAddress) extends AuthError:
    override val message: String = s"Credential '${loginEmail.value}' is disabled"

  final case class InvalidPassword(loginEmail: EmailAddress) extends AuthError:
    override val message: String = s"Invalid password for '${loginEmail.value}'"

  final case class SessionWasNotFound(sessionId: SessionId) extends AuthError:
    override val message: String = s"Session '${sessionId.value}' was not found"

  final case class SessionWasExpired(sessionId: SessionId) extends AuthError:
    override val message: String = s"Session '${sessionId.value}' has expired"

  final case class SessionWasRevoked(sessionId: SessionId) extends AuthError:
    override val message: String = s"Session '${sessionId.value}' has been revoked"

  case object UserSessionWasRequired extends AuthError:
    override val message: String = "A signed-in user session is required"

  case object ManagerSessionWasRequired extends AuthError:
    override val message: String = "A signed-in manager session is required"

  case object SessionActorDidNotMatch extends AuthError:
    override val message: String = "The current session does not match the requested actor"

  case object CurrentPasswordDidNotMatch extends AuthError:
    override val message: String = "The current password did not match"

  final case class ManagerTypeDidNotMatch(expected: AuthManagerType, actual: AuthManagerType) extends AuthError:
    override val message: String = s"Expected manager type $expected but found $actual"


