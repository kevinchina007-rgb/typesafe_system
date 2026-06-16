import type {
  AppLanguage,
  AppViewKey,
  AttractionAdminSessionResponse,
  CurrentManagerSessionResponse,
  HotelPlannerResponse,
  ManagerFlightOrderResponse,
  ManagerRefundTaskResponse,
  ManagerSessionResponse,
  ManagerTaskResponse,
  TrainAdminSessionResponse,
  UserResponse,
} from '@/lib/mvp-types/index'
import type { ManagerFlightPlannerResponse, ManagerCabinPricingInput } from '@/lib/mvp-types/manager'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'

// 绠＄悊椤甸潰鏍瑰叆鍙ｉ渶瑕佺殑 props銆?
export type ManagerPageProps = {
  currentLanguage: AppLanguage
  currentViewKey: AppViewKey
  currentManagerSession: CurrentManagerSessionResponse | null
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onManagerSessionChange: (managerSession: CurrentManagerSessionResponse | null) => void
  onSignedInUserChange: (user: UserResponse | null) => void
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

// 绠＄悊鍛樼櫥褰曠被鍨嬶紝涓庡墠鍚庣鍛藉悕淇濇寔涓€鑷淬€?
export type LoginManagerType = 'airline' | 'hotel' | 'train' | 'attraction' | 'siteAdmin'
// 璁よ瘉妯″紡鍙湁娉ㄥ唽鍜岀櫥褰曚袱绉嶃€?
export type ManagerAuthMode = 'register' | 'login'
// 闈炵珯鐐圭鐞嗗憳鐨勪笟鍔＄被鍨嬶紝渚夸簬澶嶇敤鍏ュ彛鍜屾潈闄愬垽鏂€?
export type BusinessManagerType = Exclude<LoginManagerType, 'siteAdmin'>
// 骞垮憡璧勬簮閫夐」锛屼緵绠＄悊鍚庡彴鐨勫箍鍛婃ā鍧楀鐢ㄣ€?
export type AdvertisementResourceOption = {
  value: string
  label: string
  description?: string
  departureCity?: string
  arrivalCity?: string
  departureDate?: string
  timeRange?: string
}

// 绠＄悊棣栭〉鍏ュ彛鍗＄墖鐨勫睍绀哄弬鏁般€?
export type ManagerEntryCardProps = {
  title: string
  shortTitle: string
  accentClassName: string
  imageSrc: string
  imageAlt: string
  onSelect: (authMode: ManagerAuthMode) => void
}

// 绠＄悊鍛樿璇佸崱鐗囩殑灞曠ず鍙傛暟銆?
export type ManagerAuthCardProps = {
  title: string
  registerTitle: string
  loginTitle: string
  initialAuthMode: ManagerAuthMode
  registerFields: Array<{ label: string; name: string; type?: string }>
  loginManagerType: LoginManagerType
  isBusy: boolean
  onValidationError: (message: string) => void
  onRegister: (payload: Record<string, string>) => Promise<void>
  onLogin: (payload: { managerType: LoginManagerType; email: string; password: string }) => Promise<void>
  onBack: () => void
  hideBack?: boolean
  eyebrow?: string
  allowRegister?: boolean
  translate: (translationKey: string) => string
}

// 绠＄悊椤甸潰鎺у埗鍣ㄥ椤甸潰灞傛毚闇茬殑瀹屾暣鐘舵€佸拰鍔ㄤ綔銆?
export type ManagerPageController = {
  activeManagerType: 'airline' | 'hotel' | 'train' | 'attraction' | 'siteAdmin' | null
  activeSection: 'workspace' | 'feedback' | 'advertising' | 'blogAudit' | 'advertisingReview' | 'siteAdminFeedback'
  selectedEntryType: LoginManagerType | null
  selectedEntryAuthMode: ManagerAuthMode
  currentSupplierManagerSession: ManagerSessionResponse | null
  managedFlightPlannerResponses: ManagerFlightPlannerResponse[]
  managedHotelPlannerResponses: HotelPlannerResponse[]
  currentTrainAdminSession: TrainAdminSessionResponse | null
  currentAttractionAdminSession: AttractionAdminSessionResponse | null
  managerTaskResponses: ManagerTaskResponse[]
  managerRefundTaskResponses: ManagerRefundTaskResponse[]
  hotelAdvertisementOptions: AdvertisementResourceOption[]
  attractionAdvertisementOptions: AdvertisementResourceOption[]
  advertisementResourceOptions: AdvertisementResourceOption[]
  isSiteAdmin: boolean
  canSubmitAdvertisements: boolean
  shouldShowWorkspace: boolean
  shouldShowFeedback: boolean
  shouldShowAdvertising: boolean
  shouldShowHotelProfile: boolean
  shouldShowSiteAdminPanel: boolean
  isBusy: boolean
  runAction: (action: () => Promise<void>, actionLabel: string, successLabel?: string | undefined) => Promise<void>
  selectEntry: (entryType: LoginManagerType, authMode: ManagerAuthMode) => void
  clearSelectedEntry: () => void
  setSelectedEntryAuthMode: (mode: ManagerAuthMode) => void
  logoutManager: () => Promise<void>
  ensureUserLoggedOut: () => Promise<void>
  loginSelectedManager: (managerType: LoginManagerType, email: string, password: string) => Promise<void>
  registerSelectedManager: (managerType: LoginManagerType, payload: Record<string, string>) => Promise<void>
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
  createTrainJourney: (payload: {
    trainNumber: string
    saleStartsAt: string
    stops: Array<{ stationCode: string; stationName: string; arrivalTime?: string | null; departureTime?: string | null }>
    seatInventories: Array<{ seatClass: string; totalSeats: number; saleableSeats: number; carriageCount: number; rowsPerCarriage: number; seatLayoutSpec: string }>
    segmentPrices: Array<{ fromStationCode: string; toStationCode: string; seatClass: string; amount: string; currency: string }>
    refundPolicies: Array<{ startOffsetMinutesBeforeDeparture: number; endOffsetMinutesBeforeDeparture: number; refundType: string; refundRate: string }>
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
  reloadManagerTasks: (
    filters?: {
      status: 'pending' | 'all' | 'confirmed' | 'rejected'
      resourceType: 'all' | 'flight' | 'hotel' | 'train' | 'attraction'
    },
    session?: ManagerSessionResponse,
  ) => Promise<void>
  reloadManagerRefundTasks: (session?: ManagerSessionResponse) => Promise<void>
  reloadManagedFlights: (managerId: string, filters?: {
    departureAirports?: string[]
    arrivalAirports?: string[]
    departureDate?: string
    timeRange?: string
    sortDirection?: 'asc' | 'desc'
  }) => Promise<void>
  loadManagerFlightOrders: (flightId: string) => Promise<ManagerFlightOrderResponse[]>
  reloadManagedHotels: (managerId: string) => Promise<void>
  reloadManagedTrains: (managerId: string, baseSession?: CurrentManagerSessionResponse | null) => Promise<void>
  reloadManagedAttractions: (managerId: string, baseSession?: CurrentManagerSessionResponse | null) => Promise<void>
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
  confirmTask: (payload: { orderItemId: string; note: string }) => Promise<void>
  rejectTask: (payload: { orderItemId: string; reason: string }) => Promise<void>
  batchConfirmTasks: (payload: { orderItemIds: string[]; note: string }) => Promise<void>
  batchRejectTasks: (payload: { orderItemIds: string[]; reason: string }) => Promise<void>
  approveRefundTask: (payload: { orderId: string }) => Promise<void>
  rejectRefundTask: (payload: { orderId: string }) => Promise<void>
}

