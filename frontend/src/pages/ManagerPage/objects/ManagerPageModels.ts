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

// 管理页面根入口所需的 props。
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

// 管理员登录类型，与前后端命名保持一致。
export type LoginManagerType = 'airline' | 'hotel' | 'train' | 'attraction' | 'siteAdmin'
// 认证模式只有注册和登录两种。
export type ManagerAuthMode = 'register' | 'login'
// 非站点管理员的业务类型，便于复用入口和权限判断。
export type BusinessManagerType = Exclude<LoginManagerType, 'siteAdmin'>
// 广告资源选项，供管理后台的广告模块复用。
export type AdvertisementResourceOption = {
  value: string
  label: string
  description?: string
  departureCity?: string
  arrivalCity?: string
  departureDate?: string
  timeRange?: string
}

// 管理员认证卡片的展示参数，包含标题、状态和交互回调。
export type ManagerEntryCardProps = {
  title: string
  shortTitle: string
  accentClassName: string
  imageSrc: string
  imageAlt: string
  onSelect: (authMode: ManagerAuthMode) => void
}

// 管理员认证卡片的完整配置，负责注册和登录两条入口。
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

// 管理页面控制器向页面层暴露的完整状态和动作。
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

