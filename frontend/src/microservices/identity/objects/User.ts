// Identity user domain model, kept in one file to mirror backend `User.scala`.

export type UserAccountStatus = 'PendingActivation' | 'Active' | 'Suspended' | 'Closed'

export const PendingActivation: UserAccountStatus = 'PendingActivation'
export const Active: UserAccountStatus = 'Active'
export const Suspended: UserAccountStatus = 'Suspended'
export const Closed: UserAccountStatus = 'Closed'

export function userAccountStatusFromText(value: string): UserAccountStatus {
  switch (value.trim().toLowerCase()) {
    case 'pendingactivation':
    case 'pending_activation':
      return PendingActivation
    case 'active':
      return Active
    case 'suspended':
      return Suspended
    case 'closed':
      return Closed
    default:
      return PendingActivation
  }
}

export type UserMembershipLevel = 'Standard' | 'Silver' | 'Gold' | 'Platinum'

export const Standard: UserMembershipLevel = 'Standard'
export const Silver: UserMembershipLevel = 'Silver'
export const Gold: UserMembershipLevel = 'Gold'
export const Platinum: UserMembershipLevel = 'Platinum'

export function userMembershipLevelFromText(value: string): UserMembershipLevel {
  switch (value.trim().toLowerCase()) {
    case 'silver':
      return Silver
    case 'gold':
      return Gold
    case 'platinum':
      return Platinum
    default:
      return Standard
  }
}

export type UserError =
  | {
      kind: 'UserWasNotFound'
      userId: string
      message: string
    }
  | {
      kind: 'UserWasNotFoundByEmail'
      primaryEmailAddress: string
      message: string
    }
  | {
      kind: 'UserEmailAddressAlreadyExists'
      primaryEmailAddress: string
      message: string
    }
  | {
      kind: 'InvalidUserStateTransition'
      userId: string
      currentStatus: string
      targetStatus: string
      message: string
    }
  | {
      kind: 'CannotAccruePointsForClosedUser'
      userId: string
      message: string
    }
  | {
      kind: 'InsufficientLoyaltyPoints'
      userId: string
      currentPoints: number
      requestedPoints: number
      message: string
    }
  | {
      kind: 'CannotAssignDefaultTravelerToClosedUser'
      userId: string
      message: string
    }
  | {
      kind: 'DefaultTravelerDidNotMatch'
      userId: string
      currentTravelerId: string
      requestedTravelerId: string
      message: string
    }
  | {
      kind: 'CannotUpdateAvatarForClosedUser'
      userId: string
      message: string
    }

export const userWasNotFound = (userId: string): UserError => ({
  kind: 'UserWasNotFound',
  userId,
  message: `User '${userId}' was not found`,
})

export const userWasNotFoundByEmail = (primaryEmailAddress: string): UserError => ({
  kind: 'UserWasNotFoundByEmail',
  primaryEmailAddress,
  message: `User email '${primaryEmailAddress}' was not found`,
})

export const userEmailAddressAlreadyExists = (primaryEmailAddress: string): UserError => ({
  kind: 'UserEmailAddressAlreadyExists',
  primaryEmailAddress,
  message: `User email '${primaryEmailAddress}' already exists`,
})

export type User = {
  userId: string
  primaryEmailAddress: string
  userDisplayName: string
  userPhoneNumber: string
  avatarUrl: string | null
  userAccountStatus: UserAccountStatus
  membershipLevel: UserMembershipLevel
  loyaltyPoints: number
  defaultTravelerProfileId: string | null
  registeredAt: string
}

export const userFromJson = (json: string): User => JSON.parse(json) as User

export const userToJson = (value: User): string => JSON.stringify(value)
