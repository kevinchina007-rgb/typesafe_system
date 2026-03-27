import type {
  ApiErrorResponse,
  FlightListResponse,
  FlightResponse,
  HealthResponse,
  HotelListResponse,
  HotelResponse,
  ManagerRefundTaskListResponse,
  ManagerSessionResponse,
  ManagerTaskListResponse,
  OrderListResponse,
  OrderResponse,
  TravelerListResponse,
  TravelerResponse,
  UserResponse,
} from './mvp-types'

const travelMvpApiBaseUrl = 'http://localhost:8080/api'

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
