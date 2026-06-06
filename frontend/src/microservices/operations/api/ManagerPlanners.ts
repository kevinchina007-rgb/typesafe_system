import type { CurrentManagerSessionResponse } from '@/microservices/auth/objects/CurrentManagerSessionResponse'
import type { ManagerSessionResponse } from '@/microservices/auth/objects/ManagerSessionResponse'
import type { TrainAdminSessionResponse } from '@/microservices/auth/objects/TrainAdminSessionResponse'
import type { AttractionListResponse } from '@/microservices/attraction/objects/AttractionListResponse'
import type { CreateAttractionTicketRulePlannerRequest } from '@/microservices/attraction/objects/CreateAttractionTicketRulePlannerRequest'
import type { CreateAttractionTicketSessionPlannerRequest } from '@/microservices/attraction/objects/CreateAttractionTicketSessionPlannerRequest'
import type { CreateAttractionTicketTypePlannerRequest } from '@/microservices/attraction/objects/CreateAttractionTicketTypePlannerRequest'
import type { FlightListPlannerResponse } from '@/microservices/flight/objects/FlightListPlannerResponse'
import type { FlightPlannerResponse } from '@/microservices/flight/objects/FlightPlannerResponse'
import type { HotelListPlannerResponse } from '@/microservices/hotel/objects/HotelListPlannerResponse'
import type { HotelPlannerResponse } from '@/microservices/hotel/objects/HotelPlannerResponse'
import type { ManagerBatchDecisionResponse } from '@/microservices/operations/objects/ManagerBatchDecisionResponse'
import type { ManagerFlightOrderListResponse } from '@/microservices/operations/objects/ManagerFlightOrderListResponse'
import type { ManagerRefundTaskListResponse } from '@/microservices/operations/objects/ManagerRefundTaskListResponse'
import type { ManagerTaskListResponse } from '@/microservices/operations/objects/ManagerTaskListResponse'
import type { ManagerCabinPricingInput } from '@/microservices/operations/objects/ManagerCabinPricingInput'
import type { UpdateHotelManagerProfilePlannerRequest } from '@/microservices/operations/objects/UpdateHotelManagerProfilePlannerRequest'
import type { RegisterAttractionManagerPlannerRequest } from '@/microservices/operations/objects/RegisterAttractionManagerPlannerRequest'
import type { UpdateAirlineManagerProfilePlannerRequest } from '@/microservices/operations/objects/UpdateAirlineManagerProfilePlannerRequest'
import type { TrainListResponse } from '@/microservices/train/objects/TrainListResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import { mapAttractionListResponseFromBackend, type BackendAttractionListResponse } from '@/microservices/attraction/api/AttractionResponseMappers'

export const registerAirlineManager = (payload: {
    email: string
    displayName: string
    airlineName: string
    airlineCode: string
    password: string
  }): Promise<ManagerSessionResponse> =>
    executeJsonApiRequest('/RegisterAirlineManagerPlanner', 'POST', payload)

export const registerHotelManager = (payload: {
    email: string
    displayName: string
    hotelName: string
    location: string
    password: string
  }): Promise<ManagerSessionResponse> =>
    executeJsonApiRequest('/RegisterHotelManagerPlanner', 'POST', payload)

export const registerSiteAdmin = (payload: {
    email: string
    displayName: string
    password: string
  }): Promise<CurrentManagerSessionResponse> =>
    executeJsonApiRequest('/RegisterSiteAdminPlanner', 'POST', payload)

export const updateSiteAdminManagerProfile = (payload: {
    managerId: string
    displayName: string
    logoAssetPath?: string | null
  }): Promise<ManagerSessionResponse> =>
    executeJsonApiRequest('/UpdateSiteAdminManagerProfilePlanner', 'POST', payload)

export const registerRailwayManager = (payload: { operatorCode: string; email: string; displayName: string; password: string }): Promise<TrainAdminSessionResponse> =>
    executeJsonApiRequest('/RegisterRailwayManagerPlanner', 'POST', payload)

export const registerAttractionManager = (payload: RegisterAttractionManagerPlannerRequest): Promise<ManagerSessionResponse> =>
    executeJsonApiRequest('/RegisterAttractionManagerPlanner', 'POST', payload)

export const listManagedAttractions = (managerId: string): Promise<AttractionListResponse> =>
    executeJsonApiRequest<BackendAttractionListResponse>('/ListManagedAttractionsPlanner', 'POST', { managerId }).then(response => mapAttractionListResponseFromBackend(response))

export const createAttraction = (payload: { managerId: string; attractionName: string; city: string; location: string; description: string; imageUrl?: string | null }) =>
    executeJsonApiRequest('/CreateAttractionPlanner', 'POST', payload)

export const createAttractionTicketType = (payload: CreateAttractionTicketTypePlannerRequest) =>
    executeJsonApiRequest('/CreateAttractionTicketTypePlanner', 'POST', payload)

export const createAttractionTicketSession = (payload: CreateAttractionTicketSessionPlannerRequest) =>
    executeJsonApiRequest('/CreateAttractionTicketSessionPlanner', 'POST', payload)

export const createAttractionTicketRule = (payload: CreateAttractionTicketRulePlannerRequest) =>
    executeJsonApiRequest('/CreateAttractionTicketRulePlanner', 'POST', payload)

export const listManagedTrains = (managerId: string): Promise<TrainListResponse> =>
    executeJsonApiRequest('/ListManagedTrainsPlanner', 'POST', { managerId })

export const createTrainJourney = (payload: {
    managerId: string
    trainNumber: string
    saleStartsAt: string
    stops: Array<{ stationCode: string; stationName: string; arrivalTime?: string | null; departureTime?: string | null }>
    seatInventories: Array<{ seatClass: string; totalSeats: number; saleableSeats: number; carriageCount: number; rowsPerCarriage: number; seatLayoutSpec: string }>
    segmentPrices: Array<{ fromStationCode: string; toStationCode: string; seatClass: string; amount: string; currency: string }>
    refundPolicies: Array<{ startOffsetMinutesBeforeDeparture: number; endOffsetMinutesBeforeDeparture: number; refundType: string; refundRate: string }>
  }) =>
    executeJsonApiRequest('/CreateTrainJourneyPlanner', 'POST', payload)

export const createManagerRoomType = (payload: {
    managerId: string
    roomTypeName: string
    capacity: number
    bedType: string
    nightlyPrice: string
    currency: string
    availableRooms: number
    inventoryStartDate: string
    inventoryEndDate: string
    roomImageUrl?: string | null
  }): Promise<HotelPlannerResponse> =>
    executeJsonApiRequest('/CreateManagerRoomTypePlanner', 'POST', payload)

export const listManagedHotels = (managerId: string): Promise<HotelListPlannerResponse> =>
    executeJsonApiRequest('/ListManagerHotelsPlanner', 'POST', { managerId, managerType: 'Hotel' })

export const listManagerTasks = (query: { managerId: string; managerType: string; status?: string; resourceType?: string }): Promise<ManagerTaskListResponse> =>
    executeJsonApiRequest('/ListManagerTasksPlanner', 'POST', {
      managerId: query.managerId,
      managerType: query.managerType,
      taskStatus: query.status,
      taskResourceType: query.resourceType,
    })

export const batchConfirmManagerBookingItems = (payload: { managerId: string; managerType: string; orderItemIds: string[]; note?: string | null }): Promise<ManagerBatchDecisionResponse> =>
    executeJsonApiRequest('/BatchConfirmManagerTasksPlanner', 'POST', payload)

export const batchRejectManagerBookingItems = (payload: { managerId: string; managerType: string; orderItemIds: string[]; reason: string }): Promise<ManagerBatchDecisionResponse> =>
    executeJsonApiRequest('/BatchRejectManagerTasksPlanner', 'POST', payload)

export const listManagerFlights = (
    managerId: string,
    filters: {
      departureAirports?: string[]
      arrivalAirports?: string[]
      departureDate?: string
      timeRange?: string
      sortDirection?: 'asc' | 'desc'
    } = {},
): Promise<FlightListPlannerResponse> =>
    executeJsonApiRequest('/ListManagerFlightsPlanner', 'POST', { managerId, managerType: 'Airline', ...filters })

export const listManagerFlightOrders = (managerId: string, flightId: string): Promise<ManagerFlightOrderListResponse> =>
    executeJsonApiRequest('/ListManagerFlightOrdersPlanner', 'POST', { managerId, flightId })

export const listManagerRefundTasks = (query: { managerId: string; managerType: string }): Promise<ManagerRefundTaskListResponse> =>
    executeJsonApiRequest('/ListManagerRefundTasksPlanner', 'POST', query)

export const updateAirlineManagerProfile = (payload: UpdateAirlineManagerProfilePlannerRequest): Promise<ManagerSessionResponse> =>
    executeJsonApiRequest('/UpdateAirlineManagerProfilePlanner', 'POST', payload)

export const updateHotelManagerProfile = (payload: UpdateHotelManagerProfilePlannerRequest): Promise<ManagerSessionResponse> =>
    executeJsonApiRequest('/UpdateHotelManagerProfilePlanner', 'POST', payload)

export const createManagerFlight = (payload: {
    managerId: string
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
  }): Promise<FlightPlannerResponse> =>
    executeJsonApiRequest('/CreateManagerFlightPlanner', 'POST', payload)

export const toggleManagerFlightStatus = (payload: { managerId: string; flightId: string }): Promise<FlightPlannerResponse> =>
    executeJsonApiRequest('/ToggleManagerFlightStatusPlanner', 'POST', payload)

export const confirmManagerBookingItem = (orderItemId: string, payload: { managerId: string; managerType: string; note?: string | null }) =>
    executeJsonApiRequest('/ConfirmManagerBookingItemPlanner', 'POST', { ...payload, orderItemId })

export const rejectManagerBookingItem = (orderItemId: string, payload: { managerId: string; managerType: string; reason: string }) =>
    executeJsonApiRequest('/RejectManagerBookingItemPlanner', 'POST', { ...payload, orderItemId })
