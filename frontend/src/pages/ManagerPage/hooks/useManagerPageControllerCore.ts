import { useEffect, useMemo, useState } from 'react'

import { usePageActions } from '@/pages/shared/usePageActions'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type {
  AttractionAdminSessionResponse,
  CurrentManagerSessionResponse,
  HotelPlannerResponse,
  ManagerFlightOrderResponse,
  ManagerRefundTaskResponse,
  ManagerSessionResponse,
  ManagerTaskResponse,
  TrainAdminSessionResponse,
} from '@/lib/mvp-types/index'
import type { ManagerFlightPlannerResponse } from '@/lib/mvp-types/manager'
import { toLegacyManagerSession, toManagerTypeKey } from '@/pages/ManagerPage/models/managerPageSession'
import { toActiveSection } from '../functions'
import type { AdvertisementResourceOption, LoginManagerType, ManagerAuthMode, ManagerPageProps } from '../objects'

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

export type ManagerPageControllerCoreState = {
  activeManagerType: 'airline' | 'hotel' | 'train' | 'attraction' | 'siteAdmin' | null
  activeSection: 'workspace' | 'feedback' | 'advertising' | 'blogAudit' | 'advertisingReview' | 'siteAdminFeedback'
  selectedEntryType: LoginManagerType | null
  setSelectedEntryType: (entryType: LoginManagerType | null) => void
  selectedEntryAuthMode: ManagerAuthMode
  setSelectedEntryAuthMode: (mode: ManagerAuthMode) => void
  currentSupplierManagerSession: ManagerSessionResponse | null
  setCurrentSupplierManagerSession: (session: ManagerSessionResponse | null) => void
  managedFlightPlannerResponses: ManagerFlightPlannerResponse[]
  setManagedFlightPlannerResponses: (responses: ManagerFlightPlannerResponse[]) => void
  managedHotelPlannerResponses: HotelPlannerResponse[]
  setManagedHotelPlannerResponses: (responses: HotelPlannerResponse[]) => void
  currentTrainAdminSession: TrainAdminSessionResponse | null
  setCurrentTrainAdminSession: (session: TrainAdminSessionResponse | null) => void
  currentAttractionAdminSession: AttractionAdminSessionResponse | null
  setCurrentAttractionAdminSession: (session: AttractionAdminSessionResponse | null) => void
  managerTaskResponses: ManagerTaskResponse[]
  setManagerTaskResponses: (responses: ManagerTaskResponse[]) => void
  managerRefundTaskResponses: ManagerRefundTaskResponse[]
  setManagerRefundTaskResponses: (responses: ManagerRefundTaskResponse[]) => void
  hotelAdvertisementOptions: AdvertisementResourceOption[]
  flightAdvertisementOptions: AdvertisementResourceOption[]
  trainAdvertisementOptions: AdvertisementResourceOption[]
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
  runPageAction: ReturnType<typeof usePageActions>['runPageAction']
  reloadManagerTasks: (
    filters?: {
      status: 'pending' | 'all' | 'confirmed' | 'rejected'
      resourceType: 'all' | 'flight' | 'hotel' | 'train' | 'attraction'
    },
    session?: ManagerSessionResponse,
  ) => Promise<void>
  reloadManagerRefundTasks: (session?: ManagerSessionResponse) => Promise<void>
  reloadManagedFlights: (
    managerId: string,
    filters?: {
      departureAirports?: string[]
      arrivalAirports?: string[]
      departureDate?: string
      timeRange?: string
      sortDirection?: 'asc' | 'desc'
    },
  ) => Promise<void>
  loadManagerFlightOrders: (flightId: string) => Promise<ManagerFlightOrderResponse[]>
  reloadManagedHotels: (managerId: string) => Promise<void>
  reloadManagedTrains: (managerId: string, baseSession?: CurrentManagerSessionResponse | null) => Promise<void>
  reloadManagedAttractions: (managerId: string, baseSession?: CurrentManagerSessionResponse | null) => Promise<void>
}

export function useManagerPageControllerCore({
  currentLanguage,
  currentViewKey,
  currentManagerSession,
  translate,
  onShowNotice,
}: ManagerPageProps): ManagerPageControllerCoreState {
  const activeManagerType = currentManagerSession ? toManagerTypeKey(currentManagerSession.managerType) : null
  const activeSection = toActiveSection(currentViewKey)
  const [selectedEntryType, setSelectedEntryType] = useState<LoginManagerType | null>(null)
  const [selectedEntryAuthMode, setSelectedEntryAuthMode] = useState<ManagerAuthMode>('register')
  const [currentSupplierManagerSession, setCurrentSupplierManagerSession] = useState<ManagerSessionResponse | null>(null)
  const [managedFlightPlannerResponses, setManagedFlightPlannerResponses] = useState<ManagerFlightPlannerResponse[]>([])
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
    const flightListResponse = await travelMvpApiClient.listManagerFlights({ managerId, managerType: 'Airline', ...filters })
    setManagedFlightPlannerResponses(flightListResponse.flights)
  }

  async function loadManagerFlightOrders(flightId: string): Promise<ManagerFlightOrderResponse[]> {
    if (!currentSupplierManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    const response = await travelMvpApiClient.listManagerFlightOrders({ managerId: currentSupplierManagerSession.managerId, flightId })
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
    const attractionListResponse = await travelMvpApiClient.listManagedAttractions({ managerId })
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

  const hotelAdvertisementOptions = useMemo<AdvertisementResourceOption[]>(() => {
    if (activeManagerType !== 'hotel') {
      return []
    }

    if (managedHotelPlannerResponses.length > 0) {
      return managedHotelPlannerResponses.map(hotel => ({
        value: hotel.hotelId,
        label: `${hotel.hotelName} | ${hotel.location}`,
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

  return {
    activeManagerType,
    activeSection,
    selectedEntryType,
    setSelectedEntryType,
    selectedEntryAuthMode,
    setSelectedEntryAuthMode,
    currentSupplierManagerSession,
    setCurrentSupplierManagerSession,
    managedFlightPlannerResponses,
    setManagedFlightPlannerResponses,
    managedHotelPlannerResponses,
    setManagedHotelPlannerResponses,
    currentTrainAdminSession,
    setCurrentTrainAdminSession,
    currentAttractionAdminSession,
    setCurrentAttractionAdminSession,
    managerTaskResponses,
    setManagerTaskResponses,
    managerRefundTaskResponses,
    setManagerRefundTaskResponses,
    hotelAdvertisementOptions,
    flightAdvertisementOptions,
    trainAdvertisementOptions,
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
    runPageAction,
    reloadManagerTasks,
    reloadManagerRefundTasks,
    reloadManagedFlights,
    loadManagerFlightOrders,
    reloadManagedHotels,
    reloadManagedTrains,
    reloadManagedAttractions,
  }
}
