import { useEffect, useMemo, useState } from 'react'

import { usePageActions } from '@/pages/shared/usePageActions'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type {
  AttractionAdminSessionResponse,
  CurrentManagerSessionResponse,
  FlightPlannerResponse,
  HotelPlannerResponse,
  ManagerFlightOrderResponse,
  ManagerRefundTaskResponse,
  ManagerSessionResponse,
  ManagerTaskResponse,
  TrainAdminSessionResponse,
} from '@/lib/mvp-types/index'
import { toLegacyManagerSession, toManagerTypeKey } from '@/pages/ManagerPage/models/managerPageSession'
import type { ManagerCabinPricingInput } from '@/microservices/operations/objects/ManagerCabinPricingInput'
import { toActiveSection } from '../functions'
import type { AdvertisementResourceOption, LoginManagerType, ManagerPageController, ManagerPageProps, ManagerAuthMode } from '../objects'

const airportCodeToCity: Record<string, string> = {
  PEK: '北京',
  PKX: '北京',
  SHA: '上海',
  PVG: '上海',
  CAN: '广州',
  SZX: '深圳',
  TFU: '成都',
  CTU: '成都',
  CKG: '重庆',
  HGH: '杭州',
  NKG: '南京',
  WUH: '武汉',
  XIY: '西安',
  TSN: '天津',
  CGO: '郑州',
  CSX: '长沙',
  TAO: '青岛',
  XMN: '厦门',
}

const airportCodeToName: Record<string, string> = {
  PEK: '北京首都国际机场',
  PKX: '北京大兴国际机场',
  SHA: '上海虹桥国际机场',
  PVG: '上海浦东国际机场',
  CAN: '广州白云国际机场',
  SZX: '深圳宝安国际机场',
  TFU: '成都天府国际机场',
  CTU: '成都双流国际机场',
  CKG: '重庆江北国际机场',
  HGH: '杭州萧山国际机场',
  NKG: '南京禄口国际机场',
  WUH: '武汉天河国际机场',
  XIY: '西安咸阳国际机场',
  TSN: '天津滨海国际机场',
  CGO: '郑州新郑国际机场',
  CSX: '长沙黄花国际机场',
  TAO: '青岛胶东国际机场',
  XMN: '厦门高崎国际机场',
}

function toAirportCityLabel(airportCode: string): string {
  return airportCodeToCity[airportCode.toUpperCase()] ?? airportCode
}

function toAirportNameLabel(airportCode: string): string {
  return airportCodeToName[airportCode.toUpperCase()] ?? airportCode
}

function toFlightTimeRange(isoDateTime: string): string {
  const date = new Date(isoDateTime)
  const hour = Number.isNaN(date.getTime()) ? Number(isoDateTime.slice(11, 13)) : date.getHours()
  if (hour >= 0 && hour <= 3) return '00:00-03:59'
  if (hour <= 7) return '04:00-07:59'
  if (hour <= 11) return '08:00-11:59'
  if (hour <= 15) return '12:00-15:59'
  if (hour <= 19) return '16:00-19:59'
  return '20:00-23:59'
}

function toFlightDateLabel(isoDateTime: string): string {
  const date = new Date(isoDateTime)
  if (Number.isNaN(date.getTime())) {
    return isoDateTime.slice(0, 10)
  }
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function useManagerPageController({
  currentLanguage,
  currentViewKey,
  currentManagerSession,
  signedInUser,
  translate,
  onManagerSessionChange,
  onSignedInUserChange,
  onNavigate,
  onShowNotice,
}: ManagerPageProps): ManagerPageController {
  const activeManagerType = currentManagerSession ? toManagerTypeKey(currentManagerSession.managerType) : null
  const activeSection = toActiveSection(currentViewKey)
  const [selectedEntryType, setSelectedEntryType] = useState<LoginManagerType | null>(null)
  const [selectedEntryAuthMode, setSelectedEntryAuthMode] = useState<ManagerAuthMode>('register')
  const [currentSupplierManagerSession, setCurrentSupplierManagerSession] = useState<ManagerSessionResponse | null>(null)
  const [managedFlightPlannerResponses, setManagedFlightPlannerResponses] = useState<FlightPlannerResponse[]>([])
  const [managedHotelPlannerResponses, setManagedHotelPlannerResponses] = useState<HotelPlannerResponse[]>([])
  const [currentTrainAdminSession, setCurrentTrainAdminSession] = useState<TrainAdminSessionResponse | null>(null)
  const [currentAttractionAdminSession, setCurrentAttractionAdminSession] = useState<AttractionAdminSessionResponse | null>(null)
  const [managerTaskResponses, setManagerTaskResponses] = useState<ManagerTaskResponse[]>([])
  const [managerRefundTaskResponses, setManagerRefundTaskResponses] = useState<ManagerRefundTaskResponse[]>([])
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)

  async function reloadManagerTasks(
    filters: {
      status: 'pending' | 'all' | 'confirmed' | 'rejected'
      resourceType: 'all' | 'flight' | 'hotel' | 'train' | 'attraction'
    } = { status: 'pending', resourceType: 'all' },
    session?: ManagerSessionResponse,
  ) {
    const effectiveSession = session ?? currentSupplierManagerSession
    if (!effectiveSession) {
      return
    }

    const taskListResponse = await travelMvpApiClient.listManagerTasks({
      managerId: effectiveSession.managerId,
      managerType: effectiveSession.managerType.toLowerCase(),
      status: filters.status,
      resourceType: filters.resourceType === 'all' ? undefined : filters.resourceType,
    })
    setManagerTaskResponses(taskListResponse.tasks)
  }

  async function reloadManagerRefundTasks(session?: ManagerSessionResponse) {
    const effectiveSession = session ?? currentSupplierManagerSession
    if (!effectiveSession) {
      return
    }

    const refundTaskListResponse = await travelMvpApiClient.listManagerRefundTasks({
      managerId: effectiveSession.managerId,
      managerType: effectiveSession.managerType.toLowerCase(),
    })
    setManagerRefundTaskResponses(refundTaskListResponse.tasks)
  }

  async function reloadManagedFlights(
    managerId: string,
    filters: {
      departureAirports?: string[]
      arrivalAirports?: string[]
      departureDate?: string
      timeRange?: string
      sortDirection?: 'asc' | 'desc'
    } = {},
  ) {
    const flightListResponse = await travelMvpApiClient.listManagerFlights(managerId, filters)
    setManagedFlightPlannerResponses(flightListResponse.flights)
  }

  async function loadManagerFlightOrders(flightId: string): Promise<ManagerFlightOrderResponse[]> {
    if (!currentSupplierManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    const response = await travelMvpApiClient.listManagerFlightOrders(currentSupplierManagerSession.managerId, flightId)
    return response.orders
  }

  async function reloadManagedHotels(managerId: string) {
    const hotelListResponse = await travelMvpApiClient.listManagedHotels(managerId)
    setManagedHotelPlannerResponses(hotelListResponse.hotels)
  }

  async function reloadManagedTrains(managerId: string, baseSession?: CurrentManagerSessionResponse | null) {
    const trainListResponse = await travelMvpApiClient.listManagedTrains(managerId)
    const sourceSession = baseSession ?? currentManagerSession
    if (!sourceSession) {
      return
    }

    setCurrentTrainAdminSession({
      managerId: sourceSession.managerId,
      operatorCode: sourceSession.scopeId,
      email: sourceSession.email,
      displayName: sourceSession.displayName,
      status: sourceSession.status,
      managedTrains: trainListResponse.trains,
    })
  }

  async function reloadManagedAttractions(managerId: string, baseSession?: CurrentManagerSessionResponse | null) {
    const attractionListResponse = await travelMvpApiClient.listManagedAttractions(managerId)
    const sourceSession = baseSession ?? currentManagerSession
    if (!sourceSession) {
      return
    }

    setCurrentAttractionAdminSession({
      managerId: sourceSession.managerId,
      email: sourceSession.email,
      displayName: sourceSession.displayName,
      status: sourceSession.status,
      managedAttractions: attractionListResponse.attractions,
    })
  }

  useEffect(() => {
    void (async () => {
      if (!currentManagerSession) {
        setCurrentSupplierManagerSession(null)
        setCurrentTrainAdminSession(null)
        setCurrentAttractionAdminSession(null)
        setManagedFlightPlannerResponses([])
        setManagedHotelPlannerResponses([])
        setManagerTaskResponses([])
        setManagerRefundTaskResponses([])
        return
      }

      const managerType = toManagerTypeKey(currentManagerSession.managerType)

      if (managerType === 'airline' || managerType === 'hotel' || managerType === 'attraction') {
        const legacySession = toLegacyManagerSession(currentManagerSession)
        setCurrentSupplierManagerSession(legacySession)
        setCurrentTrainAdminSession(null)
        if (managerType !== 'attraction') {
          setCurrentAttractionAdminSession(null)
        }

        await Promise.all([
          managerType === 'airline'
            ? Promise.resolve(setManagerTaskResponses([]))
            : reloadManagerTasks({ status: 'pending', resourceType: 'all' }, legacySession),
          managerType === 'airline'
            ? Promise.resolve(setManagerRefundTaskResponses([]))
            : reloadManagerRefundTasks(legacySession),
          managerType === 'airline'
            ? reloadManagedFlights(currentManagerSession.managerId)
            : Promise.resolve(setManagedFlightPlannerResponses([])),
          managerType === 'hotel'
            ? reloadManagedHotels(currentManagerSession.managerId)
            : Promise.resolve(setManagedHotelPlannerResponses([])),
          managerType === 'attraction'
            ? reloadManagedAttractions(currentManagerSession.managerId, currentManagerSession)
            : Promise.resolve(),
        ])
      } else if (managerType === 'train') {
        setCurrentSupplierManagerSession(null)
        setCurrentAttractionAdminSession(null)
        setManagedFlightPlannerResponses([])
        setManagedHotelPlannerResponses([])
        setManagerTaskResponses([])
        setManagerRefundTaskResponses([])
        await reloadManagedTrains(currentManagerSession.managerId, currentManagerSession)
      } else if (managerType === 'siteAdmin') {
        setCurrentSupplierManagerSession(null)
        setCurrentTrainAdminSession(null)
        setCurrentAttractionAdminSession(null)
        setManagedFlightPlannerResponses([])
        setManagedHotelPlannerResponses([])
        setManagerTaskResponses([])
        setManagerRefundTaskResponses([])
      }
    })()
  }, [currentManagerSession])

  async function logoutManager() {
    await travelMvpApiClient.logoutManagerAuth()
    onManagerSessionChange(null)
    setSelectedEntryType(null)
    setSelectedEntryAuthMode('register')
    setCurrentSupplierManagerSession(null)
    setCurrentTrainAdminSession(null)
    setCurrentAttractionAdminSession(null)
    setManagedFlightPlannerResponses([])
    setManagedHotelPlannerResponses([])
    setManagerTaskResponses([])
    setManagerRefundTaskResponses([])
    onNavigate('manager')
  }

  async function ensureUserLoggedOut() {
    if (!signedInUser) {
      return
    }
    await travelMvpApiClient.logoutUser()
    onSignedInUserChange(null)
  }

  async function loginSelectedManager(managerType: LoginManagerType, email: string, password: string) {
    const session = await travelMvpApiClient.loginManagerAuth({
      managerType,
      email,
      password,
    })
    onManagerSessionChange(session)
  }

  async function registerSelectedManager(managerType: LoginManagerType, payload: Record<string, string>) {
    if (managerType === 'airline') {
      await travelMvpApiClient.registerAirlineManager({
        email: payload.email.trim(),
        displayName: payload.displayName.trim(),
        airlineName: payload.airlineName.trim(),
        airlineCode: payload.airlineCode.trim(),
        password: payload.password,
      })
      return
    }

    if (managerType === 'hotel') {
      await travelMvpApiClient.registerHotelManager({
        email: payload.email.trim(),
        displayName: payload.displayName.trim(),
        hotelName: payload.hotelName.trim(),
        location: payload.location.trim(),
        password: payload.password,
      })
      return
    }

    if (managerType === 'train') {
      await travelMvpApiClient.registerRailwayManager({
        operatorCode: payload.operatorCode.trim(),
        email: payload.email.trim(),
        displayName: payload.displayName.trim(),
        password: payload.password,
      })
      return
    }

    if (managerType === 'siteAdmin') {
      await travelMvpApiClient.registerSiteAdmin({
        email: payload.email.trim(),
        displayName: payload.displayName.trim(),
        password: payload.password,
      })
      return
    }

    await travelMvpApiClient.registerAttractionManager({
      email: payload.email.trim(),
      displayName: payload.displayName.trim(),
      password: payload.password,
    })
  }

  const hotelAdvertisementOptions = useMemo<AdvertisementResourceOption[]>(() => {
    if (activeManagerType !== 'hotel') {
      return []
    }

    if (managedHotelPlannerResponses.length > 0) {
      return managedHotelPlannerResponses.map(hotel => ({
        value: hotel.hotelId,
        label: `${hotel.hotelName} 路 ${hotel.location}`,
      }))
    }

    if (!currentManagerSession) {
      return []
    }

    return [
      {
        value: currentManagerSession.scopeId,
        label: currentManagerSession.displayName,
      },
    ]
  }, [activeManagerType, currentManagerSession?.scopeId, currentManagerSession?.displayName, managedHotelPlannerResponses])

  const flightAdvertisementOptions = useMemo<AdvertisementResourceOption[]>(() => {
    if (activeManagerType !== 'airline') {
      return []
    }

    if (managedFlightPlannerResponses.length > 0) {
      return managedFlightPlannerResponses.map(flight => ({
        value: flight.flightId,
        label: `${flight.flightNumber} ${toAirportCityLabel(flight.departureAirport)} -> ${toAirportCityLabel(flight.arrivalAirport)}`,
        description: `${toAirportNameLabel(flight.departureAirport)} -> ${toAirportNameLabel(flight.arrivalAirport)} | ${toFlightDateLabel(flight.departureTime)} | ${flight.departureTime.slice(11, 16)} 起飞`,
        departureCity: toAirportCityLabel(flight.departureAirport),
        arrivalCity: toAirportCityLabel(flight.arrivalAirport),
        departureDate: toFlightDateLabel(flight.departureTime),
        timeRange: toFlightTimeRange(flight.departureTime),
      }))
    }

    if (!currentManagerSession) {
      return []
    }

    return [
      {
        value: currentManagerSession.scopeId,
        label: currentManagerSession.displayName,
      },
    ]
  }, [activeManagerType, currentManagerSession?.scopeId, currentManagerSession?.displayName, managedFlightPlannerResponses])

  const trainAdvertisementOptions = useMemo<AdvertisementResourceOption[]>(() => {
    if (activeManagerType !== 'train') {
      return []
    }

    const managedTrains = currentTrainAdminSession?.managedTrains ?? []
    if (managedTrains.length > 0) {
      return managedTrains.map(train => ({
        value: train.trainId,
        label: train.trainNumber,
      }))
    }

    if (!currentManagerSession) {
      return []
    }

    return [
      {
        value: currentManagerSession.scopeId,
        label: currentManagerSession.displayName,
      },
    ]
  }, [activeManagerType, currentManagerSession?.scopeId, currentManagerSession?.displayName, currentTrainAdminSession?.managedTrains])

  const attractionAdvertisementOptions = useMemo<AdvertisementResourceOption[]>(
    () =>
      (currentAttractionAdminSession?.managedAttractions ?? []).map(attraction => ({
        value: attraction.attractionId,
        label: attraction.attractionName,
      })),
    [currentAttractionAdminSession?.managedAttractions],
  )

  const advertisementResourceOptions = useMemo(() => {
    if (activeManagerType === 'airline') return flightAdvertisementOptions
    if (activeManagerType === 'hotel') return hotelAdvertisementOptions
    if (activeManagerType === 'train') return trainAdvertisementOptions
    if (activeManagerType === 'attraction') return attractionAdvertisementOptions
    return []
  }, [activeManagerType, attractionAdvertisementOptions, flightAdvertisementOptions, hotelAdvertisementOptions, trainAdvertisementOptions])

  const isSiteAdmin = activeManagerType === 'siteAdmin'
  const canSubmitAdvertisements = activeManagerType === 'airline' || activeManagerType === 'hotel' || activeManagerType === 'train' || activeManagerType === 'attraction'
  const shouldShowWorkspace =
    !isSiteAdmin &&
    activeSection === 'workspace' &&
    !(activeManagerType === 'hotel' && currentViewKey === 'managerProfile')
  const shouldShowFeedback = !isSiteAdmin && activeSection === 'feedback'
  const shouldShowAdvertising = !isSiteAdmin && activeSection === 'advertising' && canSubmitAdvertisements
  const shouldShowHotelProfile = !isSiteAdmin && currentViewKey === 'managerProfile' && activeManagerType === 'hotel'
  const shouldShowSiteAdminPanel = isSiteAdmin && (activeSection === 'blogAudit' || activeSection === 'advertisingReview' || activeSection === 'siteAdminFeedback')

  async function runAction(action: () => Promise<void>, actionLabel: string, successLabel?: string) {
    await runPageAction(action, actionLabel, successLabel ?? translate('notice.actionSuccess'))
  }

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
  }) {
    await travelMvpApiClient.createManagerRoomType(payload)
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
  }) {
    if (!currentAttractionAdminSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    await travelMvpApiClient.createAttraction({
      managerId: currentAttractionAdminSession.managerId,
      ...payload,
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
    activeManagerType,
    activeSection,
    selectedEntryType,
    selectedEntryAuthMode,
    currentSupplierManagerSession,
    managedFlightPlannerResponses,
    managedHotelPlannerResponses,
    currentTrainAdminSession,
    currentAttractionAdminSession,
    managerTaskResponses,
    managerRefundTaskResponses,
    hotelAdvertisementOptions,
    attractionAdvertisementOptions,
    advertisementResourceOptions,
    isSiteAdmin,
    canSubmitAdvertisements,
    shouldShowWorkspace,
    shouldShowFeedback,
    shouldShowAdvertising,
    shouldShowHotelProfile,
    shouldShowSiteAdminPanel,
    isBusy,
    runAction,
    selectEntry: (entryType, authMode) => {
      setSelectedEntryType(entryType)
      setSelectedEntryAuthMode(authMode)
    },
    clearSelectedEntry: () => {
      setSelectedEntryType(null)
      setSelectedEntryAuthMode('register')
    },
    setSelectedEntryAuthMode,
    logoutManager,
    ensureUserLoggedOut,
    loginSelectedManager,
    registerSelectedManager,
    reloadManagerTasks,
    reloadManagerRefundTasks,
    reloadManagedFlights,
    loadManagerFlightOrders,
    reloadManagedHotels,
    reloadManagedTrains,
    reloadManagedAttractions,
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
    registerAttractionManager,
    createAttraction,
    createAttractionTicketType,
    createAttractionTicketSession,
    createAttractionTicketRule,
    createTrainJourney,
  }
}
