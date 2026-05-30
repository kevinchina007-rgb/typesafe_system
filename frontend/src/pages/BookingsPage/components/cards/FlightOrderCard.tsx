import type { OrderResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import { OrderLineItemDetails } from '@/pages/BookingsPage/components/OrderLineItemDetails'
import { FlightRoutePoint } from '@/pages/BookingsPage/components/shared/FlightRoutePoint'
import { FlightTravelerBadges } from '@/pages/BookingsPage/components/shared/FlightTravelerBadges'
import { OrderItemFeedbackActions } from '@/pages/BookingsPage/components/shared/OrderItemFeedbackActions'
import { OrderPaymentActions } from '@/pages/BookingsPage/components/shared/OrderPaymentActions'
import { OrderMeta } from '@/pages/BookingsPage/components/shared/OrderMeta'
import type { OrderPanelProps } from '@/pages/BookingsPage/objects'
import {
  buildFlightOrderDisplay,
  findOrderItemReview,
  formatFlightDateTimeRange,
  hasFlightSnapshot,
  isOrderPaid,
  isOrderPayable,
  isOrderRefunded,
} from '@/pages/BookingsPage/functions'

export function FlightOrderCard({
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
  order: OrderResponse
  reviews: ReviewResponse[]
  travelers: TravelerResponse[]
  translate: OrderPanelProps['translate']
  onCancelOrder: (orderId: string) => Promise<void>
  onDeleteReview: (reviewId: string) => Promise<void>
  onOpenOrderCancellationFeedback: (orderId: string) => Promise<void>
  onOpenPayment: (order: OrderResponse) => void
}) {
  const flightItem = (order.orderLineItems ?? []).find(orderLineItem => orderLineItem.flightDetails || hasFlightSnapshot(orderLineItem.summaryLabel))
  const displayFlight = flightItem ? buildFlightOrderDisplay(flightItem) : null
  const existingReview = flightItem ? findOrderItemReview(reviews, flightItem.orderItemId) : null
  const isPayable = isOrderPayable(order.status)
  const isPaid = isOrderPaid(order.status)
  const isRefunded = isOrderRefunded(order.status)

  if (!displayFlight || !flightItem) {
    return <li className="border border-slate-200 bg-white p-5 text-slate-500 shadow-sm shadow-slate-200/40">{translate('bookings.empty')}</li>
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
              <span className="inline-flex min-h-12 items-center border border-slate-200 bg-slate-50 px-5 text-lg font-black text-slate-600">{mapBackendStatusToProductLabel(order.status, currentLanguage)}</span>
            )}
          </div>
        </div>

        <div className="grid gap-3 md:grid-cols-3">
          <OrderMeta label={translate('booking.reference')} value={order.orderId} />
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
                  bookedAmount={orderLineItem.bookedAmount}
                  bookedCurrency={orderLineItem.bookedCurrency}
                  existingReview={existingReview}
                  translate={translate}
                  onDeleteReview={onDeleteReview}
                  onOpenOrderCancellationFeedback={onOpenOrderCancellationFeedback}
                />
              </li>
            )
          })}
        </ul>
      </div>

      <OrderPaymentActions isBusy={isBusy} order={order} translate={translate} onCancelOrder={onCancelOrder} onOpenPayment={onOpenPayment} />
    </li>
  )
}
