import type { AppLanguage, FlightResponse, HotelResponse, ManagerRefundTaskResponse, ManagerSessionResponse, ManagerTaskResponse, ManagerType } from '@/lib/mvp-types/index'

export function normalizeDateTimeInput(rawValue: string): string {
  const trimmedValue = rawValue.trim()
  if (!trimmedValue) {
    return trimmedValue
  }

  const parsedDate = new Date(trimmedValue)
  if (Number.isNaN(parsedDate.getTime())) {
    return trimmedValue
  }

  return parsedDate.toISOString()
}

export type ManagerPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  managerSession: ManagerSessionResponse | null
  managedFlights: FlightResponse[]
  managedHotels: HotelResponse[]
  managerTasks: ManagerTaskResponse[]
  managerRefundTasks: ManagerRefundTaskResponse[]
  translate: (translationKey: string) => string
  onRegisterAirlineManager: (payload: {
    email: string
    displayName: string
    airlineName: string
    airlineCode: string
    password: string
  }) => Promise<void>
  onRegisterHotelManager: (payload: {
    email: string
    displayName: string
    hotelName: string
    location: string
    password: string
  }) => Promise<void>
  onCreateManagerRoomType: (payload: {
    managerId: string
    roomTypeName: string
    capacity: number
    bedType: string
    nightlyPrice: string
    currency: string
    availableRooms: number
    inventoryStartDate: string
    inventoryEndDate: string
  }) => Promise<void>
  onLoginManager: (payload: { managerType: ManagerType; email: string; password: string }) => Promise<void>
  onValidationError: (message: string) => void
  onReloadTasks: (filters: {
    status: 'pending' | 'all' | 'confirmed' | 'rejected'
    resourceType: 'all' | 'flight' | 'hotel' | 'train' | 'attraction'
  }) => Promise<void>
  onReloadRefundTasks: () => Promise<void>
  onCreateManagerFlight: (payload: {
    flightNumber: string
    departureAirport: string
    arrivalAirport: string
    departureTime: string
    arrivalTime: string
    economySeatCount: number
    economyPrice: string
    businessSeatCount: number
    businessPrice: string
    currency: string
  }) => Promise<void>
  onConfirmTask: (payload: { orderItemId: string; note: string }) => Promise<void>
  onRejectTask: (payload: { orderItemId: string; reason: string }) => Promise<void>
  onBatchConfirmTasks: (payload: { orderItemIds: string[]; note: string }) => Promise<void>
  onBatchRejectTasks: (payload: { orderItemIds: string[]; reason: string }) => Promise<void>
  onApproveRefundTask: (payload: { orderId: string }) => Promise<void>
  onRejectRefundTask: (payload: { orderId: string }) => Promise<void>
  onLogoutManager: () => void
}
