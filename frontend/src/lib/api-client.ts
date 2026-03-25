import type {
  HealthResponse,
  OrderResponse,
  TravelerListResponse,
  TravelerResponse,
  UserResponse,
} from './mvp-types'

const travelMvpApiBaseUrl = 'http://localhost:8080/api'

async function apiRequest<TResponse>(
  path: string,
  options?: RequestInit,
): Promise<TResponse> {
  const response = await fetch(`${travelMvpApiBaseUrl}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options?.headers ?? {}),
    },
    ...options,
  })

  if (!response.ok) {
    const errorText = await response.text()
    throw new Error(errorText || `HTTP ${response.status}`)
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

  getUser: (userId: string): Promise<UserResponse> =>
    apiRequest(`/users/${userId}`),

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

  listTravelers: (userId: string): Promise<TravelerListResponse> =>
    apiRequest(`/users/${userId}/travelers`),

  createOrder: (payload: {
    ownerUserId: string
    travelerId: string
    orderCurrency: string
    itemKind: string
    providerId: string
    providerLabel: string
    productId: string
    referenceCode: string
    variantLabel: string
    originCode: string
    destinationCode: string
    periodStart: string
    periodEnd: string
    quantity: number
    bookedAmount: string
  }): Promise<OrderResponse> =>
    apiRequest('/orders', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  getOrder: (orderId: string): Promise<OrderResponse> =>
    apiRequest(`/orders/${orderId}`),

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
