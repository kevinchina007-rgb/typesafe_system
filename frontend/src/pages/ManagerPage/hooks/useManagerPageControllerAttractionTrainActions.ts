import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { ManagerPageControllerCoreState } from './useManagerPageControllerCore'

export type ManagerPageAttractionTrainActions = {
  registerAttractionManager: (payload: {
    email: string
    displayName: string
    password: string
  }) => Promise<void>
  createAttraction: (payload: {
    attractionName: string
    city: string
    location: string
    description: string
    attractionImageFile?: File | null
  }) => Promise<void>
  createAttractionTicketType: (payload: {
    attractionId: string
    ticketTypeName: string
    description: string
    unitPrice: string
    currency: string
    availableFromDate: string
    availableToDate: string
    totalQuantity: number
    validWeekdays: string[]
  }) => Promise<void>
  createAttractionTicketSession: (payload: {
    attractionId: string
    ticketTypeId: string
    sessionName: string
    useDate: string
    startsAt: string
    endsAt: string
    capacity: number
  }) => Promise<void>
  createAttractionTicketRule: (payload: {
    attractionId: string
    ticketTypeId: string
    ruleType: string
    ageValue?: number | null
    minAge?: number | null
    maxAge?: number | null
    documentType?: string | null
    documentNumberPrefix?: string | null
  }) => Promise<void>
  createTrainJourney: (payload: {
    trainNumber: string
    saleStartsAt: string
    stops: Array<{ stationCode: string; stationName: string; arrivalTime?: string | null; departureTime?: string | null }>
    seatInventories: Array<{ seatClass: string; totalSeats: number; saleableSeats: number; carriageCount: number; rowsPerCarriage: number; seatLayoutSpec: string }>
    segmentPrices: Array<{ fromStationCode: string; toStationCode: string; seatClass: string; amount: string; currency: string }>
    refundPolicies: Array<{ startOffsetMinutesBeforeDeparture: number; endOffsetMinutesBeforeDeparture: number; refundType: string; refundRate: string }>
  }) => Promise<void>
}

type ManagerPageAttractionTrainActionDependencies = Pick<
  ManagerPageControllerCoreState,
  'currentAttractionAdminSession' | 'currentTrainAdminSession' | 'reloadManagedAttractions' | 'reloadManagedTrains'
> & {
  translate: (translationKey: string) => string
}

export function createManagerPageControllerAttractionTrainActions({
  currentAttractionAdminSession,
  currentTrainAdminSession,
  reloadManagedAttractions,
  reloadManagedTrains,
  translate,
}: ManagerPageAttractionTrainActionDependencies): ManagerPageAttractionTrainActions {
  async function registerAttractionManager(payload: {
    email: string
    displayName: string
    password: string
  }) {
    await travelMvpApiClient.registerAttractionManager(payload)
  }

  async function createAttraction(payload: {
    attractionName: string
    city: string
    location: string
    description: string
    attractionImageFile?: File | null
  }) {
    if (!currentAttractionAdminSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    const attractionImageFile = payload.attractionImageFile ?? null
    const attractionImageUrl = attractionImageFile ? (await travelMvpApiClient.uploadAttractionImage(attractionImageFile)).publicUrl : null
    await travelMvpApiClient.createAttraction({
      managerId: currentAttractionAdminSession.managerId,
      attractionName: payload.attractionName,
      city: payload.city,
      location: payload.location,
      description: payload.description,
      imageUrl: attractionImageUrl,
    })
    await reloadManagedAttractions(currentAttractionAdminSession.managerId)
  }

  async function createAttractionTicketType(payload: {
    attractionId: string
    ticketTypeName: string
    description: string
    unitPrice: string
    currency: string
    availableFromDate: string
    availableToDate: string
    totalQuantity: number
    validWeekdays: string[]
  }) {
    if (!currentAttractionAdminSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    await travelMvpApiClient.createAttractionTicketType({
      managerId: currentAttractionAdminSession.managerId,
      ...payload,
    })
    await reloadManagedAttractions(currentAttractionAdminSession.managerId)
  }

  async function createAttractionTicketSession(payload: {
    attractionId: string
    ticketTypeId: string
    sessionName: string
    useDate: string
    startsAt: string
    endsAt: string
    capacity: number
  }) {
    if (!currentAttractionAdminSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    await travelMvpApiClient.createAttractionTicketSession({
      managerId: currentAttractionAdminSession.managerId,
      ...payload,
    })
    await reloadManagedAttractions(currentAttractionAdminSession.managerId)
  }

  async function createAttractionTicketRule(payload: {
    attractionId: string
    ticketTypeId: string
    ruleType: string
    ageValue?: number | null
    minAge?: number | null
    maxAge?: number | null
    documentType?: string | null
    documentNumberPrefix?: string | null
  }) {
    if (!currentAttractionAdminSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    await travelMvpApiClient.createAttractionTicketRule({
      managerId: currentAttractionAdminSession.managerId,
      ...payload,
    })
    await reloadManagedAttractions(currentAttractionAdminSession.managerId)
  }

  async function createTrainJourney(payload: {
    trainNumber: string
    saleStartsAt: string
    stops: Array<{ stationCode: string; stationName: string; arrivalTime?: string | null; departureTime?: string | null }>
    seatInventories: Array<{ seatClass: string; totalSeats: number; saleableSeats: number; carriageCount: number; rowsPerCarriage: number; seatLayoutSpec: string }>
    segmentPrices: Array<{ fromStationCode: string; toStationCode: string; seatClass: string; amount: string; currency: string }>
    refundPolicies: Array<{ startOffsetMinutesBeforeDeparture: number; endOffsetMinutesBeforeDeparture: number; refundType: string; refundRate: string }>
  }) {
    if (!currentTrainAdminSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    await travelMvpApiClient.createTrainJourney({
      managerId: currentTrainAdminSession.managerId,
      ...payload,
    })
    await reloadManagedTrains(currentTrainAdminSession.managerId)
  }

  return {
    registerAttractionManager,
    createAttraction,
    createAttractionTicketType,
    createAttractionTicketSession,
    createAttractionTicketRule,
    createTrainJourney,
  }
}
