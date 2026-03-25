export type HealthResponse = {
  status: string
  service: string
  backendPort: number
}

export type UserResponse = {
  userId: string
  email: string
  nickname: string
  phone: string
  status: string
  membershipLevel: string
  points: number
  defaultTravelerProfileId: string | null
  createdAt: string
}

export type TravelerResponse = {
  travelerId: string
  ownerUserId: string
  fullName: string
  documentType: string
  documentNumber: string
  phone: string
  birthDate: string
  travelerType: string
  status: string
  isDefault: boolean
}

export type TravelerListResponse = {
  travelers: TravelerResponse[]
}

export type OrderLineItemResponse = {
  orderItemId: string
  orderItemKind: string
  orderItemStatus: string
  bookedAmount: string
  bookedCurrency: string
  summaryLabel: string
}

export type PaymentResponse = {
  paymentId: string
  paymentAmount: string
  paymentCurrency: string
  paymentMethod: string
  paymentStatus: string
  authorizedAt: string
  capturedAt: string | null
}

export type RefundResponse = {
  refundId: string
  refundAmount: string
  refundCurrency: string
  refundReason: string
  refundStatus: string
  requestedAt: string
  approvedAt: string | null
  settledAt: string | null
}

export type OrderResponse = {
  orderId: string
  buyerUserId: string
  orderType: string
  status: string
  orderCurrency: string
  totalPrice: string
  totalCapturedAmount: string
  totalSettledRefundAmount: string
  remainingRefundableAmount: string
  createdAt: string
  paidAt: string | null
  confirmedAt: string | null
  completedAt: string | null
  cancelledAt: string | null
  orderLineItems: OrderLineItemResponse[]
  orderPayments: PaymentResponse[]
  orderRefunds: RefundResponse[]
}

export type AppLanguage = 'en' | 'zh'

export type AppViewKey = 'explore' | 'account' | 'travelers' | 'bookings'

export type AppNotice = {
  id: number
  kind: 'success' | 'error' | 'info'
  title: string
  description: string
  technicalMessage?: string
}
