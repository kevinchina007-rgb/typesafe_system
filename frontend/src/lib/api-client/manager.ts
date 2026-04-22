import type {
  AttractionAdminSessionResponse,
  AttractionListResponse,
  CurrentManagerSessionResponse,
  FlightListResponse,
  FlightResponse,
  HotelListResponse,
  HotelResponse,
  ManagerBatchDecisionResponse,
  ManagerRefundTaskListResponse,
  ManagerSessionResponse,
  ManagerTaskListResponse,
  TrainAdminSessionResponse,
  TrainListResponse,
} from '../api-dtos'
import { createQueryString, executeApiRequest, executeJsonApiRequest } from '../api-transport'

export const managerApiClient = {
  loginManager: (payload: { managerType: string; email: string }): Promise<ManagerSessionResponse> =>
    executeJsonApiRequest('/manager/session/login', 'POST', payload),

  registerAirlineManager: (payload: {
    email: string
    displayName: string
    airlineName: string
    airlineCode: string
    password: string
  }): Promise<ManagerSessionResponse> =>
    executeJsonApiRequest('/manager/airline/register', 'POST', payload),

  registerHotelManager: (payload: {
    email: string
    displayName: string
    hotelName: string
    location: string
    password: string
  }): Promise<ManagerSessionResponse> =>
    executeJsonApiRequest('/manager/hotel/register', 'POST', payload),

  registerSiteAdmin: (payload: {
    email: string
    displayName: string
    password: string
  }): Promise<CurrentManagerSessionResponse> =>
    executeJsonApiRequest('/manager/site-admin/register', 'POST', payload),

  registerRailwayManager: (payload: { operatorCode: string; email: string; displayName: string; password: string }): Promise<TrainAdminSessionResponse> =>
    executeJsonApiRequest('/train-admin/managers', 'POST', payload),

  loginRailwayManager: (payload: { email: string }): Promise<TrainAdminSessionResponse> =>
    executeJsonApiRequest('/train-admin/session/login', 'POST', payload),

  registerAttractionManager: (payload: { email: string; displayName: string; password: string }): Promise<AttractionAdminSessionResponse> =>
    executeJsonApiRequest('/attraction-admin/managers', 'POST', payload),

  loginAttractionManager: (payload: { email: string }): Promise<AttractionAdminSessionResponse> =>
    executeJsonApiRequest('/attraction-admin/session/login', 'POST', payload),

  listManagedAttractions: (managerId: string): Promise<AttractionListResponse> =>
    executeApiRequest(`/attraction-admin/attractions${createQueryString({ managerId })}`),

  createAttraction: (payload: { managerId: string; attractionName: string; city: string; location: string; description: string }) =>
    executeJsonApiRequest('/attraction-admin/attractions', 'POST', payload),

  createAttractionTicketType: (payload: {
    managerId: string
    attractionId: string
    ticketTypeName: string
    description: string
    unitPrice: string
    currency: string
    availableFromDate: string
    availableToDate: string
    totalQuantity: number
    validWeekdays: string[]
  }) =>
    executeJsonApiRequest('/attraction-admin/ticket-types', 'POST', payload),

  createAttractionTicketSession: (payload: {
    managerId: string
    attractionId: string
    ticketTypeId: string
    sessionName: string
    useDate: string
    startsAt: string
    endsAt: string
    capacity: number
  }) =>
    executeJsonApiRequest('/attraction-admin/ticket-sessions', 'POST', payload),

  createAttractionTicketRule: (payload: {
    managerId: string
    attractionId: string
    ticketTypeId: string
    ruleType: string
    ageValue?: number | null
    minAge?: number | null
    maxAge?: number | null
    documentType?: string | null
    documentNumberPrefix?: string | null
  }) =>
    executeJsonApiRequest('/attraction-admin/ticket-types/rules', 'POST', payload),

  listManagedTrains: (managerId: string): Promise<TrainListResponse> =>
    executeApiRequest(`/train-admin/trains${createQueryString({ managerId })}`),

  createTrainJourney: (payload: {
    managerId: string
    trainNumber: string
    saleStartsAt: string
    stops: Array<{ stationCode: string; stationName: string; arrivalTime?: string | null; departureTime?: string | null }>
    seatInventories: Array<{ seatClass: string; totalSeats: number; saleableSeats: number; carriageCount: number; rowsPerCarriage: number; seatLayoutSpec: string }>
    segmentPrices: Array<{ fromStationCode: string; toStationCode: string; seatClass: string; amount: string; currency: string }>
    refundPolicies: Array<{ startOffsetMinutesBeforeDeparture: number; endOffsetMinutesBeforeDeparture: number; refundType: string; refundRate: string }>
  }) =>
    executeJsonApiRequest('/train-admin/trains', 'POST', payload),

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
  }): Promise<HotelResponse> =>
    executeJsonApiRequest('/manager/hotel-room-types', 'POST', payload),

  listManagedHotels: (managerId: string): Promise<HotelListResponse> =>
    executeApiRequest(`/manager/hotels${createQueryString({ managerId })}`),

  listManagerTasks: (query: { managerId: string; managerType: string; status?: string; resourceType?: string }): Promise<ManagerTaskListResponse> =>
    executeApiRequest(`/manager/tasks${createQueryString(query)}`),

  batchConfirmManagerBookingItems: (payload: { managerId: string; managerType: string; orderItemIds: string[]; note?: string | null }): Promise<ManagerBatchDecisionResponse> =>
    executeJsonApiRequest('/manager/tasks/batch-confirm', 'POST', payload),

  batchRejectManagerBookingItems: (payload: { managerId: string; managerType: string; orderItemIds: string[]; reason: string }): Promise<ManagerBatchDecisionResponse> =>
    executeJsonApiRequest('/manager/tasks/batch-reject', 'POST', payload),

  listManagerFlights: (managerId: string): Promise<FlightListResponse> =>
    executeApiRequest(`/manager/flights${createQueryString({ managerId })}`),

  listManagerRefundTasks: (query: { managerId: string; managerType: string }): Promise<ManagerRefundTaskListResponse> =>
    executeApiRequest(`/manager/refund-tasks${createQueryString(query)}`),

  createManagerFlight: (payload: {
    managerId: string
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
  }): Promise<FlightResponse> =>
    executeJsonApiRequest('/manager/flights', 'POST', payload),

  confirmManagerBookingItem: (orderItemId: string, payload: { managerId: string; managerType: string; note?: string | null }) =>
    executeJsonApiRequest(`/manager/booking-items/${orderItemId}/confirm`, 'POST', payload),

  rejectManagerBookingItem: (orderItemId: string, payload: { managerId: string; managerType: string; reason: string }) =>
    executeJsonApiRequest(`/manager/booking-items/${orderItemId}/reject`, 'POST', payload),
}
