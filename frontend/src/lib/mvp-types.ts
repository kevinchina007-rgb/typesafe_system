export type HealthResponse = {
  status: string
  service: string
  backendPort: number
}

export type ApiErrorResponse = {
  code: string
  message: string
}

export type UserResponse = {
  userId: string
  email: string
  nickname: string
  phone: string
  avatarUrl: string | null
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

export type CabinInventoryResponse = {
  inventoryId: string
  cabinClass: string
  availableSeats: number
  unitPrice: string
  currency: string
  status: string
  isBookable: boolean
}

export type FlightResponse = {
  flightId: string
  airlineId: string
  airlineName: string
  airlineCode: string
  flightNumber: string
  departureAirport: string
  arrivalAirport: string
  departureTime: string
  arrivalTime: string
  status: string
  basePrice: string
  currency: string
  createdAt: string
  cabinInventories: CabinInventoryResponse[]
}

export type FlightListResponse = {
  flights: FlightResponse[]
}

export type RoomTypeSummaryResponse = {
  roomTypeId: string
  roomTypeName: string
  capacity: number
  bedType: string
  basePrice: string
  currency: string
  status: string
  isBookableForRequestedStay: boolean
}

export type HotelResponse = {
  hotelId: string
  hotelName: string
  location: string
  status: string
  createdAt: string
  roomTypes: RoomTypeSummaryResponse[]
}

export type HotelListResponse = {
  hotels: HotelResponse[]
}

export type FlightItemDetailsResponse = {
  airlineName: string
  airlineCode: string
  flightId: string
  flightNumber: string
  departureAirport: string
  arrivalAirport: string
  departureTime: string
  arrivalTime: string
  cabinClass: string
  travelerIds: string[]
  unitPrice: string
  currency: string
}

export type HotelItemDetailsResponse = {
  hotelId: string
  hotelName: string
  location: string
  roomTypeId: string
  roomTypeName: string
  checkInDate: string
  checkOutDate: string
  guestTravelerIds: string[]
  roomCount: number
  unitPrice: string
  totalPrice: string
  currency: string
}

export type OrderLineItemResponse = {
  orderItemId: string
  orderItemKind: string
  orderItemStatus: string
  bookedAmount: string
  bookedCurrency: string
  summaryLabel: string
  flightDetails: FlightItemDetailsResponse | null
  hotelDetails: HotelItemDetailsResponse | null
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

export type AppViewKey = 'explore' | 'account' | 'travelers' | 'flights' | 'hotels' | 'bookings'

export type AppNotice = {
  id: number
  kind: 'success' | 'error' | 'info'
  title: string
  description: string
  technicalMessage?: string
}
