import type { OrderResponse, ReviewPlannerResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import { FlightTravelerBadges } from '@/pages/BookingsPage/components/shared/FlightTravelerBadges'
import { OrderItemFeedbackActions } from '@/pages/BookingsPage/components/shared/OrderItemFeedbackActions'
import { OrderPaymentActions } from '@/pages/BookingsPage/components/shared/OrderPaymentActions'
import { ArrowRight } from 'lucide-react'
import type { OrderPanelProps } from '@/pages/BookingsPage/objects'
import {
  buildTrainOrderDisplay,
  findOrderItemReview,
  formatTrainSeatLabel,
  isOrderPaid,
  isOrderPayable,
  isOrderRefunded,
  isTrainOrderLineItem,
} from '@/pages/BookingsPage/functions'

// 火车订单卡片，用于展示火车订单的车次、座位和操作入口。
export function TrainOrderCard({
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
  reviews: ReviewPlannerResponse[]
  travelers: TravelerResponse[]
  translate: OrderPanelProps['translate']
  onCancelOrder: (orderId: string) => Promise<void>
  onDeleteReview: (reviewId: string) => Promise<void>
  onOpenOrderCancellationFeedback: (orderId: string) => Promise<void>
  onOpenPayment: (order: OrderResponse) => void
}) {
  const trainLineItems = (order.orderLineItems ?? []).filter(orderLineItem => isTrainOrderLineItem(orderLineItem))
  const trainItem = trainLineItems[0] ?? null
  const displayTrain = trainItem ? buildTrainOrderDisplay(trainItem) : null
  const isPayable = isOrderPayable(order.status)
  const isPaid = isOrderPaid(order.status)
  const isRefunded = isOrderRefunded(order.status)

  if (!displayTrain || !trainItem) {
    return <li className="border border-slate-200 bg-white p-5 text-slate-500 shadow-sm shadow-slate-200/40">{translate('bookings.empty')}</li>
  }

  const seatAssignments = trainItem.trainDetails?.seatAssignments ?? displayTrain.seatAssignments

  return (
    <li className="relative border border-slate-200 bg-white p-6 shadow-sm shadow-slate-200/40">
      <span className="absolute right-6 top-5 text-xs font-medium text-slate-400">{formatIsoDateTime(order.createdAt, translate('booking.notYet'))}</span>

      <div className="grid gap-6 pr-28">
        <div className="grid gap-5 lg:grid-cols-[1.25fr_0.9fr]">
          <div className="grid gap-4">
            <div className="flex flex-wrap items-center gap-3">
              <span className="inline-flex min-h-9 items-center justify-center border border-sky-200 bg-sky-50 px-3 text-sm font-black text-sky-700">火车订单</span>
              <span className="text-sm font-semibold uppercase tracking-wide text-slate-500">{displayTrain.seatClass || '未填写席别'}</span>
            </div>

            <div className="grid gap-2">
              <h3 className="m-0 text-4xl font-black tracking-normal text-slate-950">{displayTrain.trainNumber || '未填写车次'}</h3>
              <p className="m-0 text-base font-medium text-slate-600">{`${displayTrain.departureStationName || '--'} → ${displayTrain.arrivalStationName || '--'}`}</p>
            </div>

            <div className="grid gap-4 md:grid-cols-[1fr_auto_1fr] md:items-center">
              <div className="grid gap-2 border border-cyan-100 bg-cyan-50/60 p-4">
                <span className="text-sm font-medium text-cyan-700">出发</span>
                <strong className="text-4xl font-black text-slate-950">{displayTrain.departureStationName || '--'}</strong>
                <p className="text-sm text-slate-600">{formatIsoDateTime(displayTrain.departureTime, translate('booking.notYet'))}</p>
                {displayTrain.departureStationCode ? <p className="text-xs font-medium text-slate-500">{displayTrain.departureStationCode}</p> : null}
              </div>
              <div className="hidden items-center justify-center md:flex">
                <ArrowRight className="h-14 w-14 text-sky-500 drop-shadow-sm" strokeWidth={3} />
              </div>
              <div className="grid gap-2 border border-violet-100 bg-violet-50/60 p-4">
                <span className="text-sm font-medium text-violet-700">到达</span>
                <strong className="text-4xl font-black text-slate-950">{displayTrain.arrivalStationName || '--'}</strong>
                <p className="text-sm text-slate-600">{formatIsoDateTime(displayTrain.arrivalTime, translate('booking.notYet'))}</p>
                {displayTrain.arrivalStationCode ? <p className="text-xs font-medium text-slate-500">{displayTrain.arrivalStationCode}</p> : null}
              </div>
            </div>

            <div className="border border-slate-200 bg-slate-50 p-4">
              <p className="text-sm font-semibold text-slate-500">火车座位号</p>
              <div className="mt-2 flex flex-wrap gap-2 text-sm font-medium text-slate-700">
                {seatAssignments.length > 0 ? (
                  seatAssignments.map(seatAssignment => (
                    <span key={`${seatAssignment.travelerId}-${seatAssignment.seatNo}`} className="inline-flex min-h-8 items-center border border-slate-200 bg-white px-3 py-1">
                      {formatTrainSeatLabel(seatAssignment.carriageNo, seatAssignment.seatNo, seatAssignment.seatLabel)}
                    </span>
                  ))
                ) : (
                  <span className="inline-flex min-h-8 items-center border border-slate-200 bg-white px-3 py-1 text-slate-500">未分配座位</span>
                )}
              </div>
            </div>
            <FlightTravelerBadges travelerIds={displayTrain.travelerIds} travelers={travelers} />
          </div>

          <div className="grid gap-4 border border-slate-200 bg-slate-50 p-4">
            <div className="grid gap-2">
              <span className="text-sm font-semibold text-slate-500">订单状态</span>
              <div className="flex flex-wrap items-center justify-between gap-3">
                <strong className="text-4xl font-black tracking-normal text-slate-950">{`${order.totalPrice} ${order.orderCurrency}`}</strong>
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

        <ul className="grid gap-3 border-t border-slate-200 pt-4">
          {trainLineItems.map(orderLineItem => {
            const existingReviewForLineItem = findOrderItemReview(reviews, orderLineItem.orderItemId)
            return (
              <li key={orderLineItem.orderItemId} className="grid gap-3">
                <OrderItemFeedbackActions
                  isBusy={isBusy}
                  order={order}
                  bookedAmount={orderLineItem.bookedAmount}
                  bookedCurrency={orderLineItem.bookedCurrency}
                  existingReview={existingReviewForLineItem}
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
