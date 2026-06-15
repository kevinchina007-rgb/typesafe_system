// Identity user domain helpers, kept beside `User.ts` to mirror backend structure.

import type { User, UserAccountStatus, UserError, UserMembershipLevel } from './User'
import { Active, Closed, Gold, PendingActivation, Platinum, Silver, Standard, Suspended } from './User'

export function userMembershipLevelFromPoints(loyaltyPoints: number): UserMembershipLevel {
  if (loyaltyPoints >= 50000) return Platinum
  if (loyaltyPoints >= 20000) return Gold
  if (loyaltyPoints >= 5000) return Silver
  return Standard
}

export function registerUser(
  userId: string,
  primaryEmailAddress: string,
  userDisplayName: string,
  userPhoneNumber: string,
  registeredAt: string,
): User {
  return {
    userId,
    primaryEmailAddress,
    userDisplayName,
    userPhoneNumber,
    avatarUrl: null,
    userAccountStatus: PendingActivation,
    membershipLevel: Standard,
    loyaltyPoints: 0,
    defaultTravelerProfileId: null,
    registeredAt,
  }
}

export function restorePersistedUser(
  userId: string,
  primaryEmailAddress: string,
  userDisplayName: string,
  userPhoneNumber: string,
  avatarUrl: string | null,
  userAccountStatus: UserAccountStatus,
  membershipLevel: UserMembershipLevel,
  loyaltyPoints: number,
  defaultTravelerProfileId: string | null,
  registeredAt: string,
): User {
  return {
    userId,
    primaryEmailAddress,
    userDisplayName,
    userPhoneNumber,
    avatarUrl,
    userAccountStatus,
    membershipLevel,
    loyaltyPoints,
    defaultTravelerProfileId,
    registeredAt,
  }
}

export type Result<T> =
  | { ok: true; value: T }
  | { ok: false; error: UserError }

export function activateUserAccount(user: User): Result<User> {
  if (user.userAccountStatus === PendingActivation || user.userAccountStatus === Suspended) {
    return { ok: true, value: { ...user, userAccountStatus: Active } }
  }
  return {
    ok: false,
    error: {
      kind: 'InvalidUserStateTransition',
      userId: user.userId,
      currentStatus: user.userAccountStatus,
      targetStatus: Active,
      message: `User '${user.userId}' cannot transition from ${user.userAccountStatus} to ${Active}`,
    },
  }
}

export function suspendUserAccount(user: User): Result<User> {
  if (user.userAccountStatus === Active) {
    return { ok: true, value: { ...user, userAccountStatus: Suspended } }
  }
  return {
    ok: false,
    error: {
      kind: 'InvalidUserStateTransition',
      userId: user.userId,
      currentStatus: user.userAccountStatus,
      targetStatus: Suspended,
      message: `User '${user.userId}' cannot transition from ${user.userAccountStatus} to ${Suspended}`,
    },
  }
}

export function closeUserAccount(user: User): Result<User> {
  if (user.userAccountStatus === Closed) {
    return {
      ok: false,
      error: {
        kind: 'InvalidUserStateTransition',
        userId: user.userId,
        currentStatus: user.userAccountStatus,
        targetStatus: Closed,
        message: `User '${user.userId}' cannot transition from ${user.userAccountStatus} to ${Closed}`,
      },
    }
  }
  return { ok: true, value: { ...user, userAccountStatus: Closed } }
}

export function accrueUserLoyaltyPoints(user: User, additionalPoints: number): Result<User> {
  if (user.userAccountStatus === Closed) {
    return {
      ok: false,
      error: {
        kind: 'CannotAccruePointsForClosedUser',
        userId: user.userId,
        message: `Closed user '${user.userId}' cannot accrue loyalty points`,
      },
    }
  }
  const nextPoints = user.loyaltyPoints + additionalPoints
  return {
    ok: true,
    value: { ...user, loyaltyPoints: nextPoints, membershipLevel: userMembershipLevelFromPoints(nextPoints) },
  }
}

export function redeemUserLoyaltyPoints(user: User, pointsToRedeem: number): Result<User> {
  if (user.loyaltyPoints < pointsToRedeem) {
    return {
      ok: false,
      error: {
        kind: 'InsufficientLoyaltyPoints',
        userId: user.userId,
        currentPoints: user.loyaltyPoints,
        requestedPoints: pointsToRedeem,
        message: `User '${user.userId}' has only ${user.loyaltyPoints} points and cannot redeem ${pointsToRedeem}`,
      },
    }
  }
  const nextPoints = user.loyaltyPoints - pointsToRedeem
  return {
    ok: true,
    value: { ...user, loyaltyPoints: nextPoints, membershipLevel: userMembershipLevelFromPoints(nextPoints) },
  }
}

export function assignDefaultTravelerProfile(user: User, travelerId: string): Result<User> {
  if (user.userAccountStatus === Closed) {
    return {
      ok: false,
      error: {
        kind: 'CannotAssignDefaultTravelerToClosedUser',
        userId: user.userId,
        message: `Closed user '${user.userId}' cannot assign a default traveler`,
      },
    }
  }
  return { ok: true, value: { ...user, defaultTravelerProfileId: travelerId } }
}

export function clearDefaultTravelerProfile(user: User, travelerId: string): Result<User> {
  if (user.defaultTravelerProfileId === travelerId) {
    return { ok: true, value: { ...user, defaultTravelerProfileId: null } }
  }
  if (user.defaultTravelerProfileId !== null) {
    return {
      ok: false,
      error: {
        kind: 'DefaultTravelerDidNotMatch',
        userId: user.userId,
        currentTravelerId: user.defaultTravelerProfileId,
        requestedTravelerId: travelerId,
        message: `User '${user.userId}' default traveler '${user.defaultTravelerProfileId}' did not match '${travelerId}'`,
      },
    }
  }
  return { ok: true, value: user }
}

export function updateAvatarUrl(user: User, nextAvatarUrl: string): Result<User> {
  if (user.userAccountStatus === Closed) {
    return {
      ok: false,
      error: {
        kind: 'CannotUpdateAvatarForClosedUser',
        userId: user.userId,
        message: `Closed user '${user.userId}' cannot update avatar`,
      },
    }
  }
  return { ok: true, value: { ...user, avatarUrl: nextAvatarUrl } }
}
