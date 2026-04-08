package com.typesafe.travel.auth.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum CredentialStatus:
  case Active, Disabled

enum AuthManagerType:
  case Airline, Hotel, Train, Attraction

final case class UserCredential(
    credentialId: CredentialId,
    userId: UserId,
    loginEmail: EmailAddress,
    passwordHash: String,
    status: CredentialStatus,
    createdAt: Instant,
    updatedAt: Instant,
    passwordUpdatedAt: Instant
)

final case class ManagerCredential(
    credentialId: CredentialId,
    managerType: AuthManagerType,
    managerId: ManagerId,
    loginEmail: EmailAddress,
    passwordHash: String,
    status: CredentialStatus,
    createdAt: Instant,
    updatedAt: Instant,
    passwordUpdatedAt: Instant
)

