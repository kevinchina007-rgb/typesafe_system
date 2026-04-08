import type { CurrentManagerSessionResponse, ManagerSessionResponse } from '../../lib/mvp-types'

export function toLegacyManagerSession(session: CurrentManagerSessionResponse): ManagerSessionResponse {
  return {
    managerId: session.managerId,
    managerType: session.managerType,
    email: session.email,
    displayName: session.displayName,
    status: session.status,
    scopeId: session.scopeId,
    createdAt: session.createdAt,
  }
}

export function toManagerTypeKey(managerType: string): 'airline' | 'hotel' | 'train' | 'attraction' {
  switch (managerType) {
    case 'Airline':
      return 'airline'
    case 'Hotel':
      return 'hotel'
    case 'Train':
      return 'train'
    case 'Attraction':
      return 'attraction'
    default:
      return 'airline'
  }
}
