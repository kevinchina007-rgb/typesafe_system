import type { OrderResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import { OrderLineItemDetails } from '@/pages/BookingsPage/components/OrderLineItemDetails'
import { FlightRoutePoint } from '@/pages/BookingsPage/components/shared/FlightRoutePoint'
import { FlightTravelerBadges } from '@/pages/BookingsPage/components/shared/FlightTravelerBadges'
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
  isFlightOrderLineItem,
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
  const flightLineItems = (order.orderLineItems ?? []).filter(orderLineItem => isFlightOrderLineItem(orderLineItem) || hasFlightSnapshot(orderLineItem.summaryLabel))
  const flightItem = flightLineItems[0] ?? null
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
        <div className="grid gap-5 lg:grid-cols-[1.25fr_0.9fr]">
          <div className="grid gap-4">
            <div className="flex flex-wrap items-center gap-3">
              <span className="inline-flex min-h-9 items-center justify-center border border-sky-200 bg-sky-50 px-3 text-sm font-black text-sky-700">航班订单</span>
              <span className="text-sm font-semibold uppercase tracking-wide text-slate-500">{displayFlight.cabinClass || '未填写舱位'}</span>
            </div>

            <div className="grid gap-2">
              <h3 className="m-0 text-4xl font-black tracking-normal text-slate-950">{displayFlight.airlineName || '未填写航司'}</h3>
              <p className="m-0 text-base font-medium text-slate-600">
                {displayFlight.flightNumber || '未填写航班号'}
              </p>
            </div>

            <div className="grid gap-3 md:grid-cols-[1fr_auto_1fr]">
              <FlightRoutePoint airportCode={displayFlight.departureAirport} city={displayFlight.departureCity} />
              <span className="mt-9 hidden h-px w-36 bg-slate-200 md:block" />
              <FlightRoutePoint airportCode={displayFlight.arrivalAirport} city={displayFlight.arrivalCity} />
            </div>

            <p className="m-0 text-sm font-medium text-slate-500">{formatFlightDateTimeRange(displayFlight.departureTime, displayFlight.arrivalTime)}</p>

            {isPaid || isRefunded ? <FlightTravelerBadges travelerIds={displayFlight.travelerIds} travelers={travelers} /> : null}
          </div>

          <div className="grid gap-4 border border-slate-200 bg-slate-50 p-4">
            <div className="grid gap-2">
              <span className="text-sm font-semibold text-slate-500">订单状态</span>
              <div className="flex flex-wrap items-center gap-3">
                {isPayable ? (
                  <span className="inline-flex min-h-11 items-center border border-amber-200 bg-amber-50 px-4 text-lg font-black text-amber-700">待支付</span>
                ) : isRefunded ? (
                  <span className="inline-flex min-h-11 items-center border border-sky-200 bg-sky-50 px-4 text-lg font-black text-sky-700">已退款</span>
                ) : isPaid ? (
                  <span className="inline-flex min-h-11 items-center border border-emerald-200 bg-emerald-50 px-4 text-lg font-black text-emerald-700">{translate('bookings.paid')}</span>
                ) : (
                  <span className="inline-flex min-h-11 items-center border border-slate-200 bg-slate-50 px-4 text-lg font-black text-slate-600">{mapBackendStatusToProductLabel(order.status, currentLanguage)}</span>
                )}
              </div>
            </div>

            <div className="grid gap-2 text-sm leading-6 text-slate-600">
              <p className="m-0">{`${translate('booking.reference')}: ${order.orderId}`}</p>
              <p className="m-0">{`${translate('booking.createdAt')}: ${formatIsoDateTime(order.createdAt, translate('booking.notYet'))}`}</p>
              <p className="m-0">{`${translate('booking.paidAt')}: ${formatIsoDateTime(order.paidAt, translate('booking.notYet'))}`}</p>
            </div>
          </div>
        </div>

        <div className="grid gap-3 md:grid-cols-3">
          <OrderMeta label={translate('booking.reference')} value={order.orderId} />
          <OrderMeta label={translate('booking.createdAt')} value={formatIsoDateTime(order.createdAt, translate('booking.notYet'))} />
          <OrderMeta label={translate('booking.paidAt')} value={formatIsoDateTime(order.paidAt, translate('booking.notYet'))} />
        </div>

        <ul className="grid gap-3 border-t border-slate-200 pt-4">
          {flightLineItems.map(orderLineItem => {
            return (
              <li key={orderLineItem.orderItemId} className="grid gap-3">
                <OrderLineItemDetails currentLanguage={currentLanguage} orderLineItem={orderLineItem} existingReview={findOrderItemReview(reviews, orderLineItem.orderItemId)} travelers={travelers} translate={translate} />
              </li>
            )
          })}
        </ul>

        <div className="grid gap-4 border-t border-slate-200 pt-4">
          <div className="grid gap-3 md:grid-cols-[180px_max-content]">
            <div className="flex items-center border border-slate-200 bg-white px-4 py-3">
              <strong className="text-sm font-semibold tracking-normal text-slate-950">{`${order.totalPrice} ${order.orderCurrency}`}</strong>
            </div>
            <button
              type="button"
              className="inline-flex min-h-11 w-fit items-center justify-center border border-slate-300 bg-white px-6 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
              disabled={isBusy}
              onClick={() => void onOpenOrderCancellationFeedback(order.orderId)}
            >
              申请退款及向客服反馈
            </button>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            {isPayable ? (
              <>
                <button
                  className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                  type="button"
                  disabled={isBusy}
                  onClick={() => onOpenPayment(order)}
                >
                  {translate('bookings.pay')}
                </button>
                <button
                  type="button"
                  className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                  disabled={isBusy}
                  onClick={() => void onCancelOrder(order.orderId)}
                >
                  {translate('bookings.cancel')}
                </button>
              </>
            ) : null}
            {existingReview?.canDelete ? (
              <button
                type="button"
                className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                disabled={isBusy}
                onClick={() => void onDeleteReview(existingReview.reviewId)}
              >
                {translate('reviews.delete')}
              </button>
            ) : null}
          </div>
        </div>
      </div>
    </li>
  )
}
