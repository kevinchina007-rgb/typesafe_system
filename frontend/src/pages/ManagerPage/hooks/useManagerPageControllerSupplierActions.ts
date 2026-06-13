import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { CurrentManagerSessionResponse } from '@/lib/mvp-types/index'
import type { ManagerCabinPricingInput } from '@/microservices/operations/objects/ManagerCabinPricingInput'
import type { ManagerPageProps } from '../objects'
import type { ManagerPageControllerCoreState } from './useManagerPageControllerCore'

export type ManagerPageSupplierActions = {
  registerAirlineManager: (payload: {
    email: string
    displayName: string
    airlineName: string
    airlineCode: string
    password: string
  }) => Promise<void>
  registerHotelManager: (payload: {
    email: string
    displayName: string
    hotelName: string
    location: string
    password: string
  }) => Promise<void>
  createManagerRoomType: (payload: {
    managerId: string
    roomTypeName: string
    capacity: number
    bedType: string
    nightlyPrice: string
    currency: string
    availableRooms: number
    inventoryStartDate: string
    inventoryEndDate: string
    roomImageFile?: File | null
  }) => Promise<void>
  registerRailwayManager: (payload: {
    operatorCode: string
    email: string
    displayName: string
    password: string
  }) => Promise<void>
  createManagerFlight: (payload: {
    flightNumber: string
    departureAirport: string
    arrivalAirport: string
    departureTime: string
    arrivalTime: string
    economyCabin: ManagerCabinPricingInput
    premiumEconomyCabin: ManagerCabinPricingInput
    businessCabin: ManagerCabinPricingInput
    firstCabin: ManagerCabinPricingInput
    currency: string
  }) => Promise<void>
  toggleManagerFlightStatus: (flightId: string) => Promise<void>
  updateAirlineManagerProfile: (payload: {
    displayName: string
    airlineName: string
    airlineCode: string
    logoAssetPath?: string | null
  }) => Promise<void>
  updateHotelManagerProfile: (payload: {
    managerId: string
    displayName: string
    email: string
    hotelName: string
    hotelLocation: string
  }) => Promise<void>
  confirmTask: (payload: { orderItemId: string; note: string }) => Promise<void>
  rejectTask: (payload: { orderItemId: string; reason: string }) => Promise<void>
  batchConfirmTasks: (payload: { orderItemIds: string[]; note: string }) => Promise<void>
  batchRejectTasks: (payload: { orderItemIds: string[]; reason: string }) => Promise<void>
  approveRefundTask: (payload: { orderId: string }) => Promise<void>
  rejectRefundTask: (payload: { orderId: string }) => Promise<void>
}

type ManagerPageSupplierActionDependencies = ManagerPageProps &
  Pick<
    ManagerPageControllerCoreState,
    | 'currentSupplierManagerSession'
    | 'setCurrentSupplierManagerSession'
    | 'setManagedFlightPlannerResponses'
    | 'setManagedHotelPlannerResponses'
    | 'setManagerTaskResponses'
    | 'setManagerRefundTaskResponses'
    | 'reloadManagerTasks'
    | 'reloadManagerRefundTasks'
    | 'reloadManagedFlights'
    | 'reloadManagedHotels'
  > & {
    currentManagerSession: CurrentManagerSessionResponse | null
    onManagerSessionChange: (managerSession: CurrentManagerSessionResponse | null) => void
  }

export function createManagerPageControllerSupplierActions({
  currentManagerSession,
  currentSupplierManagerSession,
  translate,
  onManagerSessionChange,
  setCurrentSupplierManagerSession,
  reloadManagerTasks,
  reloadManagerRefundTasks,
  reloadManagedFlights,
  reloadManagedHotels,
}: ManagerPageSupplierActionDependencies): ManagerPageSupplierActions {
  async function registerAirlineManager(payload: {
    email: string
    displayName: string
    airlineName: string
    airlineCode: string
    password: string
  }) {
    await travelMvpApiClient.registerAirlineManager(payload)
  }

  async function registerHotelManager(payload: {
    email: string
    displayName: string
    hotelName: string
    location: string
    password: string
  }) {
    await travelMvpApiClient.registerHotelManager(payload)
  }

  async function createManagerRoomType(payload: {
    managerId: string
    roomTypeName: string
    capacity: number
    bedType: string
    nightlyPrice: string
    currency: string
    availableRooms: number
    inventoryStartDate: string
    inventoryEndDate: string
    roomImageFile?: File | null
  }) {
    const roomImageFile = payload.roomImageFile ?? null
    const roomImageUrl = roomImageFile ? (await travelMvpApiClient.uploadHotelRoomTypeImage(roomImageFile)).publicUrl : null
    await travelMvpApiClient.createManagerRoomType({
      managerId: payload.managerId,
      roomTypeName: payload.roomTypeName,
      capacity: payload.capacity,
      bedType: payload.bedType,
      nightlyPrice: payload.nightlyPrice,
      currency: payload.currency,
      availableRooms: payload.availableRooms,
      inventoryStartDate: payload.inventoryStartDate,
      inventoryEndDate: payload.inventoryEndDate,
      roomImageUrl,
    })
    await reloadManagedHotels(payload.managerId)
  }

  async function registerRailwayManager(payload: {
    operatorCode: string
    email: string
    displayName: string
    password: string
  }) {
    await travelMvpApiClient.registerRailwayManager(payload)
  }

  async function createManagerFlight(payload: {
    flightNumber: string
    departureAirport: string
    arrivalAirport: string
    departureTime: string
    arrivalTime: string
    economyCabin: ManagerCabinPricingInput
    premiumEconomyCabin: ManagerCabinPricingInput
    businessCabin: ManagerCabinPricingInput
    firstCabin: ManagerCabinPricingInput
    currency: string
  }) {
    if (!currentSupplierManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    await travelMvpApiClient.createManagerFlight({
      managerId: currentSupplierManagerSession.managerId,
      ...payload,
    })
    await reloadManagedFlights(currentSupplierManagerSession.managerId)
  }

  async function toggleManagerFlightStatus(flightId: string) {
    if (!currentSupplierManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    await travelMvpApiClient.toggleManagerFlightStatus({
      managerId: currentSupplierManagerSession.managerId,
      flightId,
    })
    await reloadManagedFlights(currentSupplierManagerSession.managerId)
  }

  async function updateAirlineManagerProfile(payload: {
    displayName: string
    airlineName: string
    airlineCode: string
    logoAssetPath?: string | null
  }) {
    if (!currentSupplierManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    const nextSession = await travelMvpApiClient.updateAirlineManagerProfile({
      managerId: currentSupplierManagerSession.managerId,
      ...payload,
    })
    setCurrentSupplierManagerSession(nextSession)
    if (currentManagerSession) {
      onManagerSessionChange({
        ...currentManagerSession,
        displayName: nextSession.displayName,
        status: nextSession.status,
        scopeId: nextSession.scopeId,
        logoAssetPath: nextSession.logoAssetPath,
      })
    }
    await reloadManagedFlights(currentSupplierManagerSession.managerId)
  }

  async function updateHotelManagerProfile(payload: {
    managerId: string
    displayName: string
    email: string
    hotelName: string
    hotelLocation: string
  }) {
    if (!currentSupplierManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    const nextSession = await travelMvpApiClient.updateHotelManagerProfile(payload)
    setCurrentSupplierManagerSession(nextSession)
    if (currentManagerSession) {
      onManagerSessionChange({
        ...currentManagerSession,
        email: nextSession.email,
        displayName: nextSession.displayName,
        status: nextSession.status,
        scopeId: nextSession.scopeId,
      })
    }
    await reloadManagedHotels(currentSupplierManagerSession.managerId)
  }

  async function confirmTask(payload: { orderItemId: string; note: string }) {
    if (!currentSupplierManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    await travelMvpApiClient.confirmManagerBookingItem(payload.orderItemId, {
      managerId: currentSupplierManagerSession.managerId,
      managerType: currentSupplierManagerSession.managerType.toLowerCase(),
      note: payload.note.trim() || null,
    })
    await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
  }

  async function rejectTask(payload: { orderItemId: string; reason: string }) {
    if (!currentSupplierManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    await travelMvpApiClient.rejectManagerBookingItem(payload.orderItemId, {
      managerId: currentSupplierManagerSession.managerId,
      managerType: currentSupplierManagerSession.managerType.toLowerCase(),
      reason: payload.reason,
    })
    await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
  }

  async function batchConfirmTasks(payload: { orderItemIds: string[]; note: string }) {
    if (!currentSupplierManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    await travelMvpApiClient.batchConfirmManagerBookingItems({
      managerId: currentSupplierManagerSession.managerId,
      managerType: currentSupplierManagerSession.managerType.toLowerCase(),
      orderItemIds: payload.orderItemIds,
      note: payload.note.trim() || null,
    })
    await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
  }

  async function batchRejectTasks(payload: { orderItemIds: string[]; reason: string }) {
    if (!currentSupplierManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    await travelMvpApiClient.batchRejectManagerBookingItems({
      managerId: currentSupplierManagerSession.managerId,
      managerType: currentSupplierManagerSession.managerType.toLowerCase(),
      orderItemIds: payload.orderItemIds,
      reason: payload.reason,
    })
    await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
  }

  async function approveRefundTask(payload: { orderId: string }) {
    if (!currentSupplierManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    await travelMvpApiClient.approveRefund(
      payload.orderId,
      currentSupplierManagerSession.managerId,
      currentSupplierManagerSession.managerType.toLowerCase(),
    )
    await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
  }

  async function rejectRefundTask(payload: { orderId: string }) {
    if (!currentSupplierManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    await travelMvpApiClient.rejectRefund(
      payload.orderId,
      currentSupplierManagerSession.managerId,
      currentSupplierManagerSession.managerType.toLowerCase(),
    )
    await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
  }

  return {
    registerAirlineManager,
    registerHotelManager,
    createManagerRoomType,
    registerRailwayManager,
    createManagerFlight,
    toggleManagerFlightStatus,
    updateAirlineManagerProfile,
    updateHotelManagerProfile,
    confirmTask,
    rejectTask,
    batchConfirmTasks,
    batchRejectTasks,
    approveRefundTask,
    rejectRefundTask,
  }
}
