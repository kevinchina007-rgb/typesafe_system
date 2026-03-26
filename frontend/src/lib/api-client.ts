import type {
  ApiErrorResponse,
  FlightListResponse,
  FlightResponse,
  HealthResponse,
  HotelListResponse,
  HotelResponse,
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

  createOrder: (payload: {
    ownerUserId: string
    orderCurrency: string
  }): Promise<OrderResponse> =>
    apiRequest('/orders', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  addFlightItemToOrder: (
    orderId: string,
    payload: {
      buyerUserId: string
      flightId: string
      travelerIds: string[]
      cabinClass: string
    },
  ): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/flight-items`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  addHotelItemToOrder: (
    orderId: string,
    payload: {
      buyerUserId: string
      roomTypeId: string
      guestTravelerIds: string[]
      checkInDate: string
      checkOutDate: string
      roomCount: number
    },
  ): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/hotel-items`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  getOrder: (orderId: string): Promise<OrderResponse> => apiRequest(`/orders/${orderId}`),

  submitOrder: (orderId: string): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/submit`, { method: 'POST' }),

  authorizePayment: (
    orderId: string,
    payload: {
      paymentAmount: string
      paymentCurrency: string
      paymentMethod: string
    },
  ): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/payments`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  capturePayment: (orderId: string, paymentId: string): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/payments/${paymentId}/capture`, {
      method: 'POST',
    }),

  cancelOrder: (orderId: string): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/cancel`, { method: 'POST' }),

  requestRefund: (
    orderId: string,
    payload: {
      refundAmount: string
      refundCurrency: string
      refundReason: string
    },
  ): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/refunds`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  approveRefund: (orderId: string, refundId: string): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/refunds/${refundId}/approve`, {
      method: 'POST',
    }),

  settleRefund: (orderId: string, refundId: string): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}/refunds/${refundId}/settle`, {
      method: 'POST',
    }),
}
