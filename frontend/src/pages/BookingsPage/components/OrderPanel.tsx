import { formatFlightAirportLabel, formatFlightRouteCity } from '@/app/stores/models/flights/flightConstants'
import { getFlightAirlineDisplayNameByCode, getFlightAirlineLogoPathByCode } from '@/app/stores/models/flights/flightAirlineCatalog'
import { formatIsoDateTime, localizeBookingKind, localizePaymentMethod, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import type { OrderLineItemResponse, OrderResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { OrderLineItemDetails } from '@/pages/BookingsPage/components/OrderLineItemDetails'
import { buildOrderPaymentSummary, buildOrderRefundSummary, findOrderItemReview, formatTravelerIdentity, orderMatchesCategory } from '@/pages/BookingsPage/components/orderViewModel'
import type { OrderCategory, OrderPanelProps } from '@/pages/BookingsPage/components/orderViewModel'

type ReviewHandlers = Pick<OrderPanelProps, 'onDeleteReview' | 'onOpenOrderCancellationFeedback'>
type PaymentHandlers = Pick<OrderPanelProps, 'onCancelOrder' | 'onOpenPayment'>

export function OrderPanel({
  currentLanguage,
  orderCategory,
  isBusy,
  isGuestMode,
  orders,
  reviews,
  travelers,
  translate,
  onRequireLogin,
  onReloadOrders,
  onOpenPayment,
  onCancelOrder,
  onDeleteReview,
  onOpenOrderCancellationFeedback,
}: OrderPanelProps) {
  const visibleOrders = orders.filter(order => orderMatchesCategory(order, orderCategory))

  return (
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <div className="flex flex-wrap items-start justify-between gap-4 text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('topnav.orders')}</p>
          <h2>{getOrderCategoryTitle(orderCategory)}</h2>
        </div>
        <button type="button" disabled={isGuestMode || isBusy} className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={() => void onReloadOrders()}>
          {translate('bookings.refresh')}
        </button>
      </div>

      <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate(getOrderCategoryDescriptionKey(orderCategory))}</p>

      <div className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
        {isGuestMode ? (
          <div className="grid gap-4">
            <p className="text-sm leading-6 text-slate-500">{translate('bookings.guest')}</p>
            <div className="flex flex-wrap items-center gap-3">
              <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" onClick={onRequireLogin}>
                {translate('bookings.loginToPay')}
              </button>
            </div>
          </div>
        ) : null}

        {!isGuestMode && visibleOrders.length === 0 ? <p className="text-sm leading-6 text-slate-500">{translate('bookings.empty')}</p> : null}

        {visibleOrders.length > 0 ? (
          <ul className="grid gap-4">
            {visibleOrders.map(order =>
              orderCategory === 'flightOrders' ? (
                <FlightOrderCard
                  key={order.orderId}
                  currentLanguage={currentLanguage}
                  isBusy={isBusy}
                  order={order}
                  reviews={reviews}
                  travelers={travelers}
                  translate={translate}
                  onCancelOrder={onCancelOrder}
                  onDeleteReview={onDeleteReview}
                  onOpenOrderCancellationFeedback={onOpenOrderCancellationFeedback}
                  onOpenPayment={onOpenPayment}
                />
              ) : (
                <GenericOrderCard
                  key={order.orderId}
                  currentLanguage={currentLanguage}
                  isBusy={isBusy}
                  isGuestMode={isGuestMode}
                  order={order}
                  reviews={reviews}
                  travelers={travelers}
                  translate={translate}
                  onCancelOrder={onCancelOrder}
                  onDeleteReview={onDeleteReview}
                  onOpenOrderCancellationFeedback={onOpenOrderCancellationFeedback}
                  onOpenPayment={onOpenPayment}
                />
              ),
            )}
          </ul>
        ) : null}
      </div>
    </section>
  )
}

function GenericOrderCard({
  currentLanguage,
  isBusy,
  order,
  reviews,
  travelers,
  translate,
  onCancelOrder,
  onDeleteReview,
  onOpenOrderCancellationFeedback,
  onOpenPayment,
}: {
  currentLanguage: OrderPanelProps['currentLanguage']
  isBusy: boolean
  isGuestMode: boolean
  order: OrderResponse
  reviews: ReviewResponse[]
  travelers: TravelerResponse[]
  translate: OrderPanelProps['translate']
} & ReviewHandlers & PaymentHandlers) {
  return (
    <li className="border border-slate-200 bg-white p-5 shadow-sm shadow-slate-200/40">
      <div className="grid gap-5">
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          <OrderMeta label={translate('booking.reference')} value={order.orderId} />
          <OrderMeta label={translate('booking.type')} value={localizeBookingKind(order.orderType, currentLanguage)} />
          <OrderMeta label={translate('booking.status')} value={mapBackendStatusToProductLabel(order.status, currentLanguage)} />
          <OrderMeta label={translate('booking.totalPrice')} value={`${order.totalPrice} ${order.orderCurrency}`} />
          <OrderMeta label={translate('booking.createdAt')} value={formatIsoDateTime(order.createdAt, translate('booking.notYet'))} />
          <OrderMeta label={translate('booking.paidAt')} value={formatIsoDateTime(order.paidAt, translate('booking.notYet'))} />
        </div>

        <ul className="grid gap-3 border-t border-slate-200 pt-4">
          {(order.orderLineItems ?? []).map(orderLineItem => {
            const existingReview = findOrderItemReview(reviews, orderLineItem.orderItemId)
            return (
              <li key={orderLineItem.orderItemId} className="grid gap-3">
                <OrderLineItemDetails currentLanguage={currentLanguage} orderLineItem={orderLineItem} existingReview={existingReview} travelers={travelers} translate={translate} />
                <OrderItemFeedbackActions
                  isBusy={isBusy}
                  order={order}
                  orderLineItem={orderLineItem}
                  existingReview={existingReview}
                  translate={translate}
                  onDeleteReview={onDeleteReview}
                  onOpenOrderCancellationFeedback={onOpenOrderCancellationFeedback}
                />
              </li>
            )
          })}
        </ul>

        {(order.orderPayments ?? []).length > 0 ? (
          <div className="flex flex-wrap items-center gap-3">
            <span className="text-sm font-medium text-slate-500">{translate('booking.section.payments')}</span>
            <strong>{buildOrderPaymentSummary(order, localizePaymentMethod, currentLanguage)}</strong>
          </div>
        ) : null}

        {(order.orderRefunds ?? []).length > 0 ? (
          <div className="flex flex-wrap items-center gap-3">
            <span className="text-sm font-medium text-slate-500">{translate('booking.section.refunds')}</span>
            <strong>{buildOrderRefundSummary(order, currentLanguage, mapBackendStatusToProductLabel)}</strong>
          </div>
        ) : null}
      </div>

      <OrderPaymentActions isBusy={isBusy} order={order} translate={translate} onCancelOrder={onCancelOrder} onOpenPayment={onOpenPayment} />
    </li>
  )
}

function FlightOrderCard({
  isBusy,
  order,
  reviews,
  travelers,
  translate,
  onCancelOrder,
  onDeleteReview,
  onOpenOrderCancellationFeedback,
  onOpenPayment,
}: {
  currentLanguage: OrderPanelProps['currentLanguage']
  isBusy: boolean
  order: OrderResponse
  reviews: ReviewResponse[]
  travelers: TravelerResponse[]
  translate: OrderPanelProps['translate']
} & ReviewHandlers & PaymentHandlers) {
  const flightItem = (order.orderLineItems ?? []).find(orderLineItem => orderLineItem.flightDetails || parseFlightSnapshot(orderLineItem.summaryLabel))
  const displayFlight = flightItem ? buildFlightOrderDisplay(flightItem) : null
  const existingReview = flightItem ? findOrderItemReview(reviews, flightItem.orderItemId) : null
  const isPayable = isOrderPayable(order.status)
  const isPaid = isOrderPaid(order.status)
  const isRefunded = isOrderRefunded(order.status)

  if (!displayFlight || !flightItem) {
    return (
      <li className="border border-slate-200 bg-white p-5 text-slate-500 shadow-sm shadow-slate-200/40">
        {translate('bookings.empty')}
      </li>
    )
  }

  return (
    <li className="relative border border-slate-200 bg-white p-6 shadow-sm shadow-slate-200/40">
      <span className="absolute right-6 top-5 text-xs font-medium text-slate-400">{formatIsoDateTime(order.createdAt, translate('booking.notYet'))}</span>

      <div className="grid gap-6 pr-28">
        <div className="grid items-start gap-6 md:grid-cols-[1fr_auto_1fr_auto]">
          <FlightRoutePoint airportCode={displayFlight.departureAirport} city={displayFlight.departureCity} />
          <span className="mt-9 hidden h-px w-36 bg-slate-200 md:block" />
          <FlightRoutePoint airportCode={displayFlight.arrivalAirport} city={displayFlight.arrivalCity} />
          {isPaid || isRefunded ? <FlightTravelerBadges travelerIds={displayFlight.travelerIds} travelers={travelers} /> : null}
        </div>
        <p className="m-0 text-sm font-medium text-slate-500">{formatFlightDateTimeRange(displayFlight.departureTime, displayFlight.arrivalTime)}</p>

        <div className="flex flex-wrap items-end justify-between gap-5">
          <div className="grid gap-3">
            <div className="flex flex-wrap items-center gap-3 text-sm font-semibold text-slate-500">
              {displayFlight.airlineLogoPath ? <img className="h-9 w-9 border border-slate-200 bg-white object-contain p-1" src={displayFlight.airlineLogoPath} alt={displayFlight.airlineName} /> : null}
              <span>{displayFlight.airlineName}</span>
              <span>{displayFlight.flightNumber}</span>
              <button
                type="button"
                className="border-0 bg-transparent p-0 text-sm font-semibold text-blue-600 hover:text-slate-950"
                disabled={isBusy}
                onClick={() => void onOpenOrderCancellationFeedback(order.orderId)}
              >
                申请退款及向客服反馈
              </button>
              {existingReview?.canDelete ? (
                <button type="button" className="border-0 bg-transparent p-0 text-sm font-semibold text-slate-500 hover:text-slate-950" disabled={isBusy} onClick={() => void onDeleteReview(existingReview.reviewId)}>
                  {translate('reviews.delete')}
                </button>
              ) : null}
            </div>
            {displayFlight.cabinClass ? <span className="text-sm font-medium text-slate-500">{displayFlight.cabinClass}</span> : null}
          </div>

          <div className="flex flex-wrap items-center gap-4">
            <strong className="text-3xl font-black tracking-normal text-slate-950">{`${order.totalPrice} ${order.orderCurrency}`}</strong>
            {isPayable ? (
              <button className="min-h-14 border-0 bg-pink-500 px-8 text-xl font-black text-white shadow-none hover:bg-pink-600 disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy} onClick={() => onOpenPayment(order)}>
                {translate('bookings.pay')}
              </button>
            ) : isRefunded ? (
              <span className="inline-flex min-h-12 items-center border border-sky-200 bg-sky-50 px-5 text-lg font-black text-sky-700">已退款</span>
            ) : isPaid ? (
              <span className="inline-flex min-h-12 items-center border border-emerald-200 bg-emerald-50 px-5 text-lg font-black text-emerald-700">{translate('bookings.paid')}</span>
            ) : (
              <span className="inline-flex min-h-12 items-center border border-slate-200 bg-slate-50 px-5 text-lg font-black text-slate-600">{mapBackendStatusToProductLabel(order.status, 'zh')}</span>
            )}
          </div>
        </div>
      </div>

      {isPayable ? (
        <div className="mt-5">
          <button type="button" className="border-0 bg-transparent p-0 text-sm font-semibold text-slate-500 hover:text-slate-950" disabled={isBusy} onClick={() => void onCancelOrder(order.orderId)}>
            {translate('bookings.cancel')}
          </button>
        </div>
      ) : null}

    </li>
  )
}

function OrderItemFeedbackActions({
  isBusy,
  order,
  orderLineItem,
  existingReview,
  translate,
  onDeleteReview,
  onOpenOrderCancellationFeedback,
}: {
  isBusy: boolean
  order: OrderResponse
  orderLineItem: OrderLineItemResponse
  existingReview: ReviewResponse | null
  translate: OrderPanelProps['translate']
} & ReviewHandlers) {
  return (
    <div className="flex flex-wrap items-center gap-3">
      <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{`${orderLineItem.bookedAmount} ${orderLineItem.bookedCurrency}`}</span>
      <button
        type="button"
        className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
        disabled={isBusy}
        onClick={() => void onOpenOrderCancellationFeedback(order.orderId)}
      >
        申请退款及向客服反馈
      </button>
      {existingReview?.canDelete ? (
        <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={() => void onDeleteReview(existingReview.reviewId)}>
          {translate('reviews.delete')}
        </button>
      ) : null}
    </div>
  )
}

function OrderPaymentActions({
  isBusy,
  order,
  translate,
  onCancelOrder,
  onOpenPayment,
}: {
  isBusy: boolean
  order: OrderResponse
  translate: OrderPanelProps['translate']
} & PaymentHandlers) {
  return (
    <div className="mt-4 flex flex-wrap items-center gap-3">
      {isOrderPayable(order.status) ? (
        <>
          <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy} onClick={() => onOpenPayment(order)}>
            {translate('bookings.pay')}
          </button>
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={() => void onCancelOrder(order.orderId)}>
            {translate('bookings.cancel')}
          </button>
        </>
      ) : null}
    </div>
  )
}

function FlightRoutePoint({ airportCode, city }: { airportCode: string; city: string }) {
  return (
    <div className="grid gap-2">
      <span className="text-6xl font-black tracking-normal text-slate-950">{city}</span>
      <span className="text-2xl font-medium leading-tight text-slate-600">{formatFlightAirportLabel(airportCode)}</span>
    </div>
  )
}

function isOrderPayable(status: string) {
  return status === 'Draft' || status === 'PendingPayment' || status === 'PendingSelection'
}

function isOrderPaid(status: string) {
  return status === 'Confirmed' || status === 'Paid' || status === 'Booked'
}

function isOrderRefunded(status: string) {
  return status === 'Refunded'
}

function FlightTravelerBadges({ travelerIds, travelers }: { travelerIds: string[]; travelers: TravelerResponse[] }) {
  if (travelerIds.length === 0) {
    return null
  }

  return (
    <div className="grid min-w-44 gap-2 justify-self-start md:justify-self-end">
      {travelerIds.map(travelerId => {
        const label = formatTravelerIdentity(travelers, travelerId)
        return (
          <div key={travelerId} className="flex items-center gap-2 text-sm font-semibold text-slate-600">
            <span className="inline-flex h-9 w-9 items-center justify-center border border-slate-300 bg-slate-50 text-base font-black text-slate-700">{label.slice(0, 1)}</span>
            <span className="max-w-32 truncate">{label}</span>
          </div>
        )
      })}
    </div>
  )
}

function OrderMeta({ label, value }: { label: string; value: string }) {
  return (
    <div className="grid gap-1">
      <span className="text-sm font-medium text-slate-500">{label}</span>
      <strong className="break-words text-lg font-bold text-slate-950">{value}</strong>
    </div>
  )
}

type FlightSnapshotSummary = {
  airlineName?: string
  airlineCode?: string
  flightNumber?: string
  flightId?: string
  departureAirport?: string
  arrivalAirport?: string
  departureTime?: string
  arrivalTime?: string
  cabinClass?: string
  travelerIds?: string[]
}

function parseFlightSnapshot(summaryLabel: string): FlightSnapshotSummary | null {
  if (!summaryLabel.trim().startsWith('{')) {
    return null
  }

  try {
    const parsed = JSON.parse(summaryLabel) as Record<string, unknown>
    return {
      airlineName: getStringField(parsed, 'airlineName'),
      airlineCode: getStringField(parsed, 'airlineCode'),
      flightNumber: getStringField(parsed, 'flightNumber'),
      flightId: getStringField(parsed, 'flightId'),
      departureAirport: getStringField(parsed, 'departureAirport') ?? getStringField(parsed, 'departureAirportCode'),
      arrivalAirport: getStringField(parsed, 'arrivalAirport') ?? getStringField(parsed, 'arrivalAirportCode'),
      departureTime: getStringField(parsed, 'departureTime') ?? getStringField(parsed, 'departureAt'),
      arrivalTime: getStringField(parsed, 'arrivalTime') ?? getStringField(parsed, 'arrivalAt'),
      cabinClass: getStringField(parsed, 'cabinClass'),
      travelerIds: getStringArrayField(parsed, 'travelerIds'),
    }
  } catch {
    return null
  }
}

function buildFlightOrderDisplay(orderLineItem: OrderLineItemResponse) {
  const snapshot = parseFlightSnapshot(orderLineItem.summaryLabel)
  const flightDetails = orderLineItem.flightDetails
  const departureAirport = flightDetails?.departureAirport ?? snapshot?.departureAirport ?? ''
  const arrivalAirport = flightDetails?.arrivalAirport ?? snapshot?.arrivalAirport ?? ''
  const airlineCode = flightDetails?.airlineCode ?? snapshot?.airlineCode ?? 'MU'

  return {
    airlineName: getFlightAirlineDisplayNameByCode(airlineCode, flightDetails?.airlineName ?? snapshot?.airlineName ?? '航空公司'),
    airlineCode,
    airlineLogoPath: getFlightAirlineLogoPathByCode(airlineCode, `/images/airlines/${airlineCode}.svg`),
    flightNumber: flightDetails?.flightNumber ?? snapshot?.flightNumber ?? snapshot?.flightId ?? '',
    departureAirport,
    arrivalAirport,
    departureCity: formatFlightRouteCity(departureAirport),
    arrivalCity: formatFlightRouteCity(arrivalAirport),
    departureTime: flightDetails?.departureTime ?? snapshot?.departureTime ?? '',
    arrivalTime: flightDetails?.arrivalTime ?? snapshot?.arrivalTime ?? '',
    cabinClass: flightDetails?.cabinClass ?? snapshot?.cabinClass ?? '',
    travelerIds: flightDetails?.travelerIds ?? snapshot?.travelerIds ?? [],
  }
}

function getStringField(record: Record<string, unknown>, key: string) {
  const value = record[key]
  return typeof value === 'string' && value.trim() ? value : undefined
}

function getStringArrayField(record: Record<string, unknown>, key: string) {
  const value = record[key]
  return Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string' && item.trim().length > 0) : []
}

function formatFlightClock(value: string) {
  if (!value) {
    return '--:--'
  }
  const date = new Date(value)
  if (!Number.isNaN(date.getTime())) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false })
  }
  const matchedTime = value.match(/(\d{2}):(\d{2})/)
  return matchedTime ? `${matchedTime[1]}:${matchedTime[2]}` : '--:--'
}

function formatFlightDateTimeRange(departureTime: string, arrivalTime: string) {
  const dateLabel = formatFlightDate(departureTime)
  return `${dateLabel} ${formatFlightClock(departureTime)} - ${formatFlightClock(arrivalTime)}`
}

function formatFlightDate(value: string) {
  if (!value) {
    return '--/--'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value.slice(0, 10).replaceAll('-', '/')
  }
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', weekday: 'short' })
}

function getOrderCategoryTitle(orderCategory: OrderCategory) {
  if (orderCategory === 'flightOrders') {
    return '航班订单'
  }
  if (orderCategory === 'hotelOrders') {
    return '酒店订单'
  }
  if (orderCategory === 'trainOrders') {
    return '火车票订单'
  }
  return '景点门票订单'
}

function getOrderCategoryDescriptionKey(orderCategory: OrderCategory) {
  if (orderCategory === 'flightOrders') {
    return 'bookings.flightDescription'
  }
  if (orderCategory === 'hotelOrders') {
    return 'bookings.hotelDescription'
  }
  if (orderCategory === 'trainOrders') {
    return 'bookings.trainDescription'
  }
  return 'bookings.attractionDescription'
}
