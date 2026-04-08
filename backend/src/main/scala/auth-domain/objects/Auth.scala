package com.typesafe.travel.auth.domain

import com.typesafe.travel.shared.kernel.*

enum AuthError(val message: String) extends DomainError:
  case PasswordWasEmpty extends AuthError("Password cannot be empty")
  case PasswordWasTooShort extends AuthError("Password must be at least 10 characters")
  case PasswordWasTooWeak extends AuthError("Password must include letters and numbers and avoid weak defaults")
  case UserCredentialWasNotFoundByEmail(loginEmail: EmailAddress) extends AuthError(s"User credential '${loginEmail.value}' was not found")
  case UserCredentialWasNotFoundByUserId(userId: UserId) extends AuthError(s"User credential for '${userId.value}' was not found")
  case ManagerCredentialWasNotFoundByEmail(managerType: AuthManagerType, loginEmail: EmailAddress)
      extends AuthError(s"$managerType credential '${loginEmail.value}' was not found")
  case ManagerCredentialWasNotFound(managerType: AuthManagerType, managerId: ManagerId)
      extends AuthError(s"$managerType credential for '${managerId.value}' was not found")
  case UserCredentialAlreadyExists(loginEmail: EmailAddress) extends AuthError(s"User credential '${loginEmail.value}' already exists")
  case ManagerCredentialAlreadyExists(managerType: AuthManagerType, loginEmail: EmailAddress)
      extends AuthError(s"$managerType credential '${loginEmail.value}' already exists")
  case CredentialWasDisabled(loginEmail: EmailAddress) extends AuthError(s"Credential '${loginEmail.value}' is disabled")
  case InvalidPassword(loginEmail: EmailAddress) extends AuthError(s"Invalid password for '${loginEmail.value}'")
  case SessionWasNotFound(sessionId: SessionId) extends AuthError(s"Session '${sessionId.value}' was not found")
  case SessionWasExpired(sessionId: SessionId) extends AuthError(s"Session '${sessionId.value}' has expired")
  case SessionWasRevoked(sessionId: SessionId) extends AuthError(s"Session '${sessionId.value}' has been revoked")
  case UserSessionWasRequired extends AuthError("A signed-in user session is required")
  case ManagerSessionWasRequired extends AuthError("A signed-in manager session is required")
  case SessionActorDidNotMatch extends AuthError("The current session does not match the requested actor")
  case CurrentPasswordDidNotMatch extends AuthError("The current password did not match")
  case ManagerTypeDidNotMatch(expected: AuthManagerType, actual: AuthManagerType)
      extends AuthError(s"Expected manager type $expected but found $actual")
