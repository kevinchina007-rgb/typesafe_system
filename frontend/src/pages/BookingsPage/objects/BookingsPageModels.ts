import type { AppLanguage, AppViewKey, OrderLineItemResponse, OrderResponse, PaymentLinkResponse, ReviewPlannerResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'

// BookingsPage 使用的订单分类，只对应当前页面会展示的几种订单。
export type OrderCategory = Extract<AppViewKey, 'flightOrders' | 'hotelOrders' | 'trainOrders' | 'attractionOrders'>

// BookingsPage 的页面级参数，负责承接语言、用户和导航能力。
export type BookingsPageProps = {
  currentLanguage: AppLanguage
  orderCategory: OrderCategory
  isSessionReady: boolean
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

// 订单侧边栏/操作区需要的一组动作，统一从页面控制器里下发。
export type BookingOrderActions = {
  onReloadOrders: () => Promise<void>
  onOpenPayment: (order: OrderResponse) => void
  onCancelOrder: (orderId: string) => Promise<void>
  onRequestRefund: (orderId: string, refundReason: string) => Promise<void>
  onDeleteReview: (reviewId: string) => Promise<void>
  onOpenOrderCancellationFeedback: (orderId: string) => Promise<void>
}

// 支付方式枚举值，只保留页面上真正可选的几种方式。
export type PaymentMethodValue = 'alipay' | 'wechat-pay' | 'nailong-pay'

// 订单面板参数，承接订单、评价、出行人和各类操作入口。
export type OrderPanelProps = {
  currentLanguage: AppLanguage
  orderCategory: OrderCategory
  isBusy: boolean
  isGuestMode: boolean
  orders: OrderResponse[]
  reviews: ReviewPlannerResponse[]
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onReloadOrders: () => Promise<void>
  onOpenPayment: (order: OrderResponse) => void
  onCancelOrder: (orderId: string) => Promise<void>
  onDeleteReview: (reviewId: string) => Promise<void>
  onOpenOrderCancellationFeedback: (orderId: string) => Promise<void>
}

// 支付弹窗参数，负责承接订单、出行人和支付回调。
export type PaymentModalProps = {
  isOpen: boolean
  order: OrderResponse | null
  travelers: TravelerResponse[]
  isBusy: boolean
  onClose: () => void
  onCreatePaymentLink: (payload: { orderId: string; paymentMethod: PaymentMethodValue }) => Promise<PaymentLinkResponse>
  onConfirmPayment: (payload: { orderId: string; paymentMethod: PaymentMethodValue; travelerIds?: string[] }) => Promise<void>
}

// 订单行项目详情参数，用于展示单个订单项的补充信息。
export type OrderLineItemDetailsProps = {
  currentLanguage: AppLanguage
  orderLineItem: OrderLineItemResponse
  existingReview: ReviewPlannerResponse | null
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
}

// 航班订单快照摘要，只负责解析显示所需的最小字段。
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

// 酒店订单快照摘要，只负责解析显示所需的最小字段。
export type HotelSnapshotSummary = {
  hotelName?: string
  hotelLocation?: string
  roomTypeName?: string
  roomCount?: number
  checkInDate?: string
  checkOutDate?: string
  travelerIds: string[]
}

// 火车订单快照摘要，只负责解析显示所需的最小字段。
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

// 航班订单展示模型，供页面直接渲染卡片内容。
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

// 酒店订单展示模型，供页面直接渲染卡片内容。
export type HotelOrderDisplay = {
  hotelName: string
  hotelLocation: string
  roomTypeName: string
  roomCount: number
  checkInDate: string
  checkOutDate: string
  guestTravelerIds: string[]
}

// 火车订单展示模型，供页面直接渲染卡片内容。
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

// BookingsPage 控制器对页面暴露的完整状态和动作集合。
export type BookingsPageController = {
  orders: OrderResponse[]
  reviews: ReviewPlannerResponse[]
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
