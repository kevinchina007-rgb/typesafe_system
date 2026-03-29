import type {
  AttractionAdminSessionResponse,
  AttractionListResponse,
  AttractionResponse,
  ApiErrorResponse,
  FlightListResponse,
  FlightResponse,
  HealthResponse,
  HotelListResponse,
  HotelResponse,
  TrainAdminSessionResponse,
  TrainListResponse,
  TrainResponse,
  ManagerRefundTaskListResponse,
  ManagerSessionResponse,
  ManagerTaskListResponse,
  OrderListResponse,
  OrderResponse,
  TravelerListResponse,
  TravelerResponse,
  UserResponse,
} from './mvp-types'
import { getTravelBackendOrigin } from './runtime-config'

const travelBackendOrigin = getTravelBackendOrigin()
const travelMvpApiBaseUrl = `${travelBackendOrigin}/api`

function formatApiErrorMessage(apiErrorResponse: ApiErrorResponse, status: number): string {
  return `${apiErrorResponse.code}|${apiErrorResponse.message}|HTTP ${status}`
}

async function apiRequest<TResponse>(path: string, options?: RequestInit): Promise<TResponse> {
  const isMultipartBody = typeof FormData !== 'undefined' && options?.body instanceof FormData
  const response = await fetch(`${travelMvpApiBaseUrl}${path}`, {
    headers: isMultipartBody
      ? {
          ...(options?.headers ?? {}),
        }
      : {
          'Content-Type': 'application/json',
          ...(options?.headers ?? {}),
        },
    ...options,
  })

  if (!response.ok) {
    let responseBodyText = ''
    try {
      responseBodyText = await response.text()
      const parsedApiError = JSON.parse(responseBodyText) as Partial<ApiErrorResponse>
      if (parsedApiError.code && parsedApiError.message) {
        throw new Error(formatApiErrorMessage(parsedApiError as ApiErrorResponse, response.status))
      }
    } catch (parsingError) {
      if (parsingError instanceof Error && parsingError.message.includes('|')) {
        throw parsingError
      }
    }

    throw new Error(responseBodyText || `HTTP ${response.status}`)
  }

  if (response.status === 204) {
    return undefined as TResponse
  }

  return (await response.json()) as TResponse
}

export const travelMvpApiClient = {
  getHealth: (): Promise<HealthResponse> => apiRequest('/health'),

  createUser: (payload: {
    email: string
    nickname: string
    phone: string
  }): Promise<UserResponse> =>
    apiRequest('/users', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  loginUser: (payload: { email: string }): Promise<UserResponse> =>
    apiRequest('/session/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  getUser: (userId: string): Promise<UserResponse> => apiRequest(`/users/${userId}`),

  uploadUserAvatar: (userId: string, avatarFile: File): Promise<UserResponse> => {
    const formData = new FormData()
    formData.set('avatar', avatarFile)
    return apiRequest(`/users/${userId}/avatar`, {
      method: 'POST',
      body: formData,
    })
  },

  createTraveler: (
    userId: string,
    payload: {
      fullName: string
      documentType: string
      documentNumber: string
      phone: string
      birthDate: string
      seatPreference: string
      mealPreference: string
      accessibilityRequestNotes: string | null
      emergencyContactName: string | null
      emergencyContactPhoneNumber: string | null
      isDefaultTraveler: boolean
    },
  ): Promise<TravelerResponse> =>
    apiRequest(`/users/${userId}/travelers`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  updateTraveler: (
    userId: string,
    travelerId: string,
    payload: {
      fullName: string
      documentType: string
      documentNumber: string
      phone: string
      birthDate: string
      seatPreference: string
      mealPreference: string
      accessibilityRequestNotes: string | null
      emergencyContactName: string | null
      emergencyContactPhoneNumber: string | null
      isDefaultTraveler: boolean
    },
  ): Promise<TravelerResponse> =>
    apiRequest(`/users/${userId}/travelers/${travelerId}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    }),

  listTravelers: (userId: string): Promise<TravelerListResponse> =>
    apiRequest(`/users/${userId}/travelers`),

  deleteTraveler: (userId: string, travelerId: string): Promise<void> =>
    apiRequest(`/users/${userId}/travelers/${travelerId}`, {
      method: 'DELETE',
    }),

  listFlights: (query: {
    departureAirport?: string
    arrivalAirport?: string
    date?: string
  }): Promise<FlightListResponse> => {
    const searchParams = new URLSearchParams()
    if (query.departureAirport) {
      searchParams.set('departureAirport', query.departureAirport)
    }
    if (query.arrivalAirport) {
      searchParams.set('arrivalAirport', query.arrivalAirport)
    }
    if (query.date) {
      searchParams.set('date', query.date)
    }
    const suffix = searchParams.toString() ? `?${searchParams.toString()}` : ''
    return apiRequest(`/flights${suffix}`)
  },

  getFlight: (flightId: string): Promise<FlightResponse> => apiRequest(`/flights/${flightId}`),

  listHotels: (query: {
    location?: string
    checkInDate?: string
    checkOutDate?: string
  }): Promise<HotelListResponse> => {
    const searchParams = new URLSearchParams()
    if (query.location) {
      searchParams.set('location', query.location)
    }
    if (query.checkInDate) {
      searchParams.set('checkInDate', query.checkInDate)
    }
    if (query.checkOutDate) {
      searchParams.set('checkOutDate', query.checkOutDate)
    }
    const suffix = searchParams.toString() ? `?${searchParams.toString()}` : ''
    return apiRequest(`/hotels${suffix}`)
  },

  getHotel: (hotelId: string, query?: { checkInDate?: string; checkOutDate?: string }): Promise<HotelResponse> => {
    const searchParams = new URLSearchParams()
    if (query?.checkInDate) {
      searchParams.set('checkInDate', query.checkInDate)
    }
    if (query?.checkOutDate) {
      searchParams.set('checkOutDate', query.checkOutDate)
    }
    const suffix = searchParams.toString() ? `?${searchParams.toString()}` : ''
    return apiRequest(`/hotels/${hotelId}${suffix}`)
  },

  listTrains: (query: {
    fromStation?: string
    toStation?: string
    date?: string
  }): Promise<TrainListResponse> => {
    const searchParams = new URLSearchParams()
    if (query.fromStation) searchParams.set('fromStation', query.fromStation)
    if (query.toStation) searchParams.set('toStation', query.toStation)
    if (query.date) searchParams.set('date', query.date)
    const suffix = searchParams.toString() ? `?${searchParams.toString()}` : ''
    return apiRequest(`/trains${suffix}`)
  },

  getTrain: (trainId: string): Promise<TrainResponse> => apiRequest(`/trains/${trainId}`),

  listAttractions: (query?: {
    city?: string
  }): Promise<AttractionListResponse> => {
    const searchParams = new URLSearchParams()
    if (query?.city) {
      searchParams.set('city', query.city)
    }
    const suffix = searchParams.toString() ? `?${searchParams.toString()}` : ''
    return apiRequest(`/attractions${suffix}`)
  },

  getAttraction: (attractionId: string): Promise<AttractionResponse> => apiRequest(`/attractions/${attractionId}`),

  createOrder: (payload: { ownerUserId: string; orderCurrency: string }): Promise<OrderResponse> =>
    apiRequest('/orders', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  addTrainItemToOrder: (
    orderId: string,
    payload: {
      buyerUserId: string
      orderId: string
      trainId: string
      travelerIds: string[]
      fromStationCode: string
      toStationCode: string
      seatClass: string
    },
  ): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/train-items`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  addAttractionItemToOrder: (
    orderId: string,
    payload: {
      buyerUserId: string
      orderId: string
      attractionId: string
      ticketTypeId: string
      travelerIds: string[]
      useDate: string
    },
  ): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/attraction-items`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  createFlightOrder: (
    payload: {
      buyerUserId: string
      flightId: string
      travelerIds: string[]
      cabinClass: string
    },
  ): Promise<OrderResponse> =>
    apiRequest('/flights/book', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  createHotelOrder: (
    payload: {
      buyerUserId: string
      roomTypeId: string
      guestTravelerIds: string[]
      checkInDate: string
      checkOutDate: string
      roomCount: number
    },
  ): Promise<OrderResponse> =>
    apiRequest('/hotels/book', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  getOrder: (orderId: string): Promise<OrderResponse> => apiRequest(`/orders/${orderId}`),
  listOrders: (userId: string): Promise<OrderListResponse> => apiRequest(`/users/${userId}/orders`),

  loginManager: (payload: { managerType: string; email: string }): Promise<ManagerSessionResponse> =>
    apiRequest('/manager/session/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  registerAirlineManager: (payload: {
    email: string
    displayName: string
    airlineName: string
    airlineCode: string
  }): Promise<ManagerSessionResponse> =>
    apiRequest('/manager/airline/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  registerHotelManager: (payload: {
    email: string
    displayName: string
    hotelName: string
    location: string
  }): Promise<ManagerSessionResponse> =>
    apiRequest('/manager/hotel/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  registerRailwayManager: (payload: {
    operatorCode: string
    email: string
    displayName: string
  }): Promise<TrainAdminSessionResponse> =>
    apiRequest('/train-admin/managers', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  loginRailwayManager: (payload: { email: string }): Promise<TrainAdminSessionResponse> =>
    apiRequest('/train-admin/session/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  registerAttractionManager: (payload: {
    email: string
    displayName: string
  }): Promise<AttractionAdminSessionResponse> =>
    apiRequest('/attraction-admin/managers', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  loginAttractionManager: (payload: { email: string }): Promise<AttractionAdminSessionResponse> =>
    apiRequest('/attraction-admin/session/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  listManagedAttractions: (managerId: string): Promise<AttractionListResponse> =>
    apiRequest(`/attraction-admin/attractions?managerId=${encodeURIComponent(managerId)}`),

  createAttraction: (payload: {
    managerId: string
    attractionName: string
    city: string
    location: string
    description: string
  }): Promise<AttractionResponse> =>
    apiRequest('/attraction-admin/attractions', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  createAttractionTicketType: (payload: {
    managerId: string
    attractionId: string
    ticketTypeName: string
    description: string
    unitPrice: string
    currency: string
  }): Promise<AttractionResponse> =>
    apiRequest('/attraction-admin/ticket-types', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

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
  }): Promise<AttractionResponse> =>
    apiRequest('/attraction-admin/ticket-types/rules', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  listManagedTrains: (managerId: string): Promise<TrainListResponse> =>
    apiRequest(`/train-admin/trains?managerId=${encodeURIComponent(managerId)}`),

  createTrainJourney: (payload: {
    managerId: string
    trainNumber: string
    saleStartsAt: string
    stops: Array<{ stationCode: string; stationName: string; arrivalTime?: string | null; departureTime?: string | null }>
    seatInventories: Array<{ seatClass: string; totalSeats: number; saleableSeats: number }>
    segmentPrices: Array<{ fromStationCode: string; toStationCode: string; seatClass: string; amount: string; currency: string }>
    refundPolicies: Array<{ startOffsetMinutesBeforeDeparture: number; endOffsetMinutesBeforeDeparture: number; refundType: string; refundRate: string }>
  }): Promise<TrainResponse> =>
    apiRequest('/train-admin/trains', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

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
    apiRequest('/manager/hotel-room-types', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  listManagerTasks: (query: {
    managerId: string
    managerType: string
    status?: string
  }): Promise<ManagerTaskListResponse> => {
    const searchParams = new URLSearchParams()
    searchParams.set('managerId', query.managerId)
    searchParams.set('managerType', query.managerType)
    if (query.status) {
      searchParams.set('status', query.status)
    }
    return apiRequest(`/manager/tasks?${searchParams.toString()}`)
  },

  listManagerRefundTasks: (query: {
    managerId: string
    managerType: string
  }): Promise<ManagerRefundTaskListResponse> => {
    const searchParams = new URLSearchParams()
    searchParams.set('managerId', query.managerId)
    searchParams.set('managerType', query.managerType)
    return apiRequest(`/manager/refund-tasks?${searchParams.toString()}`)
  },

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
    apiRequest('/manager/flights', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  confirmManagerBookingItem: (
    orderItemId: string,
    payload: {
      managerId: string
      managerType: string
      note?: string | null
    },
  ): Promise<OrderResponse> =>
    apiRequest(`/manager/booking-items/${orderItemId}/confirm`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  rejectManagerBookingItem: (
    orderItemId: string,
    payload: {
      managerId: string
      managerType: string
      reason: string
    },
  ): Promise<OrderResponse> =>
    apiRequest(`/manager/booking-items/${orderItemId}/reject`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  payOrder: (
    orderId: string,
    payload: {
      paymentMethod: string
      paymentSucceeded: boolean
    },
  ): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/pay`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  cancelOrder: (orderId: string): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/cancel`, { method: 'POST' }),

  requestRefund: (
    orderId: string,
    payload: {
      refundReason: string
    },
  ): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/refunds`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  approveRefund: (orderId: string, managerId: string, managerType: string): Promise<OrderResponse> =>
    apiRequest(`/manager/orders/${orderId}/refund/approve?managerId=${encodeURIComponent(managerId)}&managerType=${encodeURIComponent(managerType)}`, {
      method: 'POST',
    }),

  rejectRefund: (orderId: string, managerId: string, managerType: string): Promise<OrderResponse> =>
    apiRequest(`/manager/orders/${orderId}/refund/reject?managerId=${encodeURIComponent(managerId)}&managerType=${encodeURIComponent(managerType)}`, {
      method: 'POST',
    }),
}
