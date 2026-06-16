import type { AppLanguage, HotelPlannerResponse, ManagerRefundTaskResponse, ManagerSessionResponse, ManagerTaskResponse, ManagerType } from '@/lib/mvp-types/index'
import type { ManagerFlightPlannerResponse, ManagerCabinPricingInput, ManagerFlightOrderResponse } from '@/lib/mvp-types/manager'

// 鎶婂彲鑳借緭鍏ョ殑鏃ユ湡鏃堕棿缁熶竴褰掍竴鎴?ISO 瀛楃涓诧紝淇濊瘉鍚庣鎺ユ敹鏍煎紡涓€鑷淬€?
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

// 鑸┖绠＄悊鍚庡彴鍐呴儴浣跨敤鐨勫垎鍖哄悕绉般€?
export type AirlineWorkspaceSection = 'createFlight' | 'flightManagement' | 'userFeedback' | 'managerProfile'

// 绠＄悊鍚庡彴涓婚潰鏉块渶瑕佺殑鍏ㄩ儴 props锛屽敖閲忛泦涓湪杩欓噷瀵归綈銆?
export type ManagerPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  managerSession: ManagerSessionResponse | null
  managedFlights: ManagerFlightPlannerResponse[]
  managedHotels: HotelPlannerResponse[]
  managerTasks: ManagerTaskResponse[]
  managerRefundTasks: ManagerRefundTaskResponse[]
  initialAirlineSection?: AirlineWorkspaceSection
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
    roomImageFile?: File | null
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
    economyCabin: ManagerCabinPricingInput
    premiumEconomyCabin: ManagerCabinPricingInput
    businessCabin: ManagerCabinPricingInput
    firstCabin: ManagerCabinPricingInput
    currency: string
  }) => Promise<void>
  onToggleManagerFlightStatus: (flightId: string) => Promise<void>
  onSearchManagerFlights: (payload: {
    departureAirports?: string[]
    arrivalAirports?: string[]
    departureDate?: string
    timeRange?: string
    sortDirection?: 'asc' | 'desc'
  }) => Promise<void>
  onLoadManagerFlightOrders: (flightId: string) => Promise<ManagerFlightOrderResponse[]>
  onUpdateAirlineManagerProfile: (payload: {
    displayName: string
    airlineName: string
    airlineCode: string
    logoAssetPath?: string | null
  }) => Promise<void>
  onUpdateHotelManagerProfile: (payload: {
    managerId: string
    displayName: string
    email: string
    hotelName: string
    hotelLocation: string
  }) => Promise<void>
  onConfirmTask: (payload: { orderItemId: string; note: string }) => Promise<void>
  onRejectTask: (payload: { orderItemId: string; reason: string }) => Promise<void>
  onBatchConfirmTasks: (payload: { orderItemIds: string[]; note: string }) => Promise<void>
  onBatchRejectTasks: (payload: { orderItemIds: string[]; reason: string }) => Promise<void>
  onApproveRefundTask: (payload: { orderId: string }) => Promise<void>
  onRejectRefundTask: (payload: { orderId: string }) => Promise<void>
  onLogoutManager: () => void
}

