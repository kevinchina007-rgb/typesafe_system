import type { AppLanguage, AppViewKey, OrderLineItemResponse, OrderResponse, PaymentLinkResponse, ReviewResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'

export type OrderCategory = Extract<AppViewKey, 'flightOrders' | 'hotelOrders' | 'trainOrders' | 'attractionOrders'>

export type BookingsPageProps = {
  currentLanguage: AppLanguage
  orderCategory: OrderCategory
  isSessionReady: boolean
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

export type BookingOrderActions = {
  onReloadOrders: () => Promise<void>
  onOpenPayment: (order: OrderResponse) => void
  onCancelOrder: (orderId: string) => Promise<void>
  onRequestRefund: (orderId: string, refundReason: string) => Promise<void>
  onDeleteReview: (reviewId: string) => Promise<void>
  onOpenOrderCancellationFeedback: (orderId: string) => Promise<void>
}

export type PaymentMethodValue = 'alipay' | 'wechat-pay' | 'nailong-pay'

export type OrderPanelProps = {
  currentLanguage: AppLanguage
  orderCategory: OrderCategory
  isBusy: boolean
  isGuestMode: boolean
  orders: OrderResponse[]
  reviews: ReviewResponse[]
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onReloadOrders: () => Promise<void>
  onOpenPayment: (order: OrderResponse) => void
  onCancelOrder: (orderId: string) => Promise<void>
  onDeleteReview: (reviewId: string) => Promise<void>
  onOpenOrderCancellationFeedback: (orderId: string) => Promise<void>
}

export type PaymentModalProps = {
  isOpen: boolean
  order: OrderResponse | null
  travelers: TravelerResponse[]
  isBusy: boolean
  onClose: () => void
  onCreatePaymentLink: (payload: { orderId: string; paymentMethod: PaymentMethodValue }) => Promise<PaymentLinkResponse>
  onConfirmPayment: (payload: { orderId: string; paymentMethod: PaymentMethodValue; travelerIds?: string[] }) => Promise<void>
}

export type OrderLineItemDetailsProps = {
  currentLanguage: AppLanguage
  orderLineItem: OrderLineItemResponse
  existingReview: ReviewResponse | null
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
}

export type FlightSnapshotSummary = {
  airlineName?: string
  flightNumber?: string
  flightId?: string
  departureAirport?: string
  arrivalAirport?: string
  departureAirportCode?: string
  arrivalAirportCode?: string
  cabinClass?: string
  travelerIds: string[]
}

export type HotelSnapshotSummary = {
  hotelName?: string
  hotelLocation?: string
  roomTypeName?: string
  roomCount?: number
  checkInDate?: string
  checkOutDate?: string
  travelerIds: string[]
}

export type TrainSnapshotSummary = {
  trainId?: string
  trainNumber?: string
  seatClass?: string
  departureStation?: string
  departureStationCode?: string
  arrivalStation?: string
  arrivalStationCode?: string
  departureTime?: string
  arrivalTime?: string
  requestedSeatPreference?: string
  seatAssignments?: Array<{
    travelerId: string
    carriageNo: number
    seatNo: string
    seatLabel: string
  }>
  travelerIds: string[]
  unitPrice?: string
  totalPrice?: string
  currency?: string
}

export type FlightOrderDisplay = {
  airlineName: string
  airlineLogoPath: string | null
  flightNumber: string
  departureAirport: string
  departureCity: string
  departureTime: string
  arrivalAirport: string
  arrivalCity: string
  arrivalTime: string
  cabinClass: string | null
  travelerIds: string[]
}

export type HotelOrderDisplay = {
  hotelName: string
  hotelLocation: string
  roomTypeName: string
  roomCount: number
  checkInDate: string
  checkOutDate: string
  guestTravelerIds: string[]
}

export type TrainOrderDisplay = {
  trainId: string
  trainNumber: string
  departureStationName: string
  departureStationCode: string
  arrivalStationName: string
  arrivalStationCode: string
  departureTime: string
  arrivalTime: string
  seatClass: string | null
  requestedSeatPreference: string | null
  travelerIds: string[]
  totalPrice: string
  currency: string
  unitPrice: string | null
  seatAssignments: Array<{
    travelerId: string
    carriageNo: number
    seatNo: string
    seatLabel: string
  }>
  reservationStatus: string | null
  reservationExpiresAt: string | null
}

export type BookingsPageController = {
  orders: OrderResponse[]
  reviews: ReviewResponse[]
  travelers: TravelerResponse[]
  pendingPaymentOrder: OrderResponse | null
  isAuthDialogOpen: boolean
  isBusy: boolean
  isGuestMode: boolean
  setPendingPaymentOrder: (order: OrderResponse | null) => void
  setIsAuthDialogOpen: (open: boolean) => void
  onRequireLogin: () => void
  onAuthDialogClose: () => void
  onAuthDialogConfirm: () => void
  onOpenPayment: (order: OrderResponse) => void
  onReloadOrders: () => Promise<void>
  onCancelOrder: (orderId: string) => Promise<void>
  onRequestRefund: (orderId: string, refundReason: string) => Promise<void>
  onDeleteReview: (reviewId: string) => Promise<void>
  onOpenOrderCancellationFeedback: (orderId: string) => Promise<void>
  onCreatePaymentLink: (payload: { orderId: string; paymentMethod: PaymentMethodValue }) => Promise<PaymentLinkResponse>
  onConfirmPayment: (payload: { orderId: string; paymentMethod: PaymentMethodValue; travelerIds?: string[] }) => Promise<void>
}
