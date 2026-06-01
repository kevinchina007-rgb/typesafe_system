import type { OrderResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, localizeTrainSeatClass, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import { OrderLineItemDetails } from '@/pages/BookingsPage/components/OrderLineItemDetails'
import { OrderItemFeedbackActions } from '@/pages/BookingsPage/components/shared/OrderItemFeedbackActions'
import { OrderMeta } from '@/pages/BookingsPage/components/shared/OrderMeta'
import { OrderPaymentActions } from '@/pages/BookingsPage/components/shared/OrderPaymentActions'
import type { OrderPanelProps } from '@/pages/BookingsPage/objects'
import { buildTrainOrderDisplay, formatTrainDateTimeRange, formatTrainSeatLabel, isOrderPaid, isOrderPayable, isOrderRefunded, findOrderItemReview, isTrainOrderLineItem } from '@/pages/BookingsPage/functions'

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
  reviews: ReviewResponse[]
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
  const travelerIds = displayTrain.travelerIds

  return (
    <li className="relative border border-slate-200 bg-white p-6 shadow-sm shadow-slate-200/40">
      <span className="absolute right-6 top-5 text-xs font-medium text-slate-400">{formatIsoDateTime(order.createdAt, translate('booking.notYet'))}</span>

      <div className="grid gap-6 pr-28">
        <div className="grid gap-5">
          <div className="grid gap-4">
            <div className="flex flex-wrap items-center gap-3">
              <span className="inline-flex min-h-9 items-center justify-center border border-sky-200 bg-sky-50 px-3 text-sm font-black text-sky-700">火车订单</span>
              <strong className="text-2xl font-black tracking-normal text-slate-950">{displayTrain.trainNumber || '未填写车次'}</strong>
              {displayTrain.seatClass ? (
                <span className="inline-flex min-h-9 items-center justify-center border border-violet-200 bg-violet-50 px-3 text-sm font-semibold text-violet-700">
                  {localizeTrainSeatClass(displayTrain.seatClass, currentLanguage)}
                </span>
              ) : null}
            </div>

            <div className="grid gap-3 xl:grid-cols-[1fr_auto_1fr]">
              <div className="grid gap-2 border border-cyan-100 bg-cyan-50/60 p-4">
                <span className="text-sm font-medium text-cyan-700">出发</span>
                <strong className="text-2xl font-black text-slate-950">{displayTrain.departureStationName || '--'}</strong>
                <p className="text-sm text-slate-600">{formatIsoDateTime(displayTrain.departureTime, translate('booking.notYet'))}</p>
                {displayTrain.departureStationCode ? <p className="text-xs font-medium text-slate-500">{displayTrain.departureStationCode}</p> : null}
              </div>
              <div className="hidden xl:grid place-items-center text-3xl font-black text-slate-300">→</div>
              <div className="grid gap-2 border border-violet-100 bg-violet-50/60 p-4">
                <span className="text-sm font-medium text-violet-700">到达</span>
                <strong className="text-2xl font-black text-slate-950">{displayTrain.arrivalStationName || '--'}</strong>
                <p className="text-sm text-slate-600">{formatIsoDateTime(displayTrain.arrivalTime, translate('booking.notYet'))}</p>
                {displayTrain.arrivalStationCode ? <p className="text-xs font-medium text-slate-500">{displayTrain.arrivalStationCode}</p> : null}
              </div>
            </div>

            <div className="grid gap-3 md:grid-cols-2">
              <div className="border border-slate-200 bg-white p-4">
                <p className="text-sm font-semibold text-slate-500">席别与旅客</p>
                <div className="mt-2 flex flex-wrap items-center gap-2">
                  <span className="inline-flex min-h-9 items-center border border-slate-300 bg-slate-50 px-3 text-sm font-semibold text-slate-700">
                    {displayTrain.seatClass ? localizeTrainSeatClass(displayTrain.seatClass, currentLanguage) : '未填写席别'}
                  </span>
                  <span className="inline-flex min-h-9 items-center border border-slate-300 bg-slate-50 px-3 text-sm font-semibold text-slate-700">
                    {`旅客 ${travelerIds.length}`}
                  </span>
                  {seatAssignments.length > 0 ? (
                    <span className="inline-flex min-h-9 items-center border border-amber-200 bg-amber-50 px-3 text-sm font-semibold text-amber-700">
                      {seatAssignments
                        .map(seatAssignment => formatTrainSeatLabel(seatAssignment.carriageNo, seatAssignment.seatNo, seatAssignment.seatLabel))
                        .join(' | ')}
                    </span>
                  ) : (
                    <span className="inline-flex min-h-9 items-center border border-slate-200 bg-slate-50 px-3 text-sm font-semibold text-slate-600">
                      未分配座位
                    </span>
                  )}
                </div>
              </div>

              <div className="border border-slate-200 bg-white p-4">
                <p className="text-sm font-semibold text-slate-500">价格</p>
                <div className="mt-2 flex items-end justify-between gap-3">
                  <div className="grid gap-1">
                    {displayTrain.unitPrice ? <p className="text-sm text-slate-500">{`单价 ${displayTrain.unitPrice} ${displayTrain.currency}`}</p> : null}
                    <strong className="text-3xl font-black tracking-normal text-slate-950">{`${displayTrain.totalPrice} ${displayTrain.currency}`}</strong>
                  </div>
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
            </div>

            {seatAssignments.length > 0 ? (
              <div className="border border-slate-200 bg-slate-50 p-4">
                <p className="text-sm font-semibold text-slate-500">座位安排</p>
                <div className="mt-2 flex flex-wrap gap-2 text-sm font-medium text-slate-700">
                  {seatAssignments.map(seatAssignment => (
                    <span key={`${seatAssignment.travelerId}-${seatAssignment.seatNo}`} className="inline-flex min-h-8 items-center border border-slate-200 bg-white px-3">
                      {formatTrainSeatLabel(seatAssignment.carriageNo, seatAssignment.seatNo, seatAssignment.seatLabel)}
                    </span>
                  ))}
                </div>
              </div>
            ) : null}

            <p className="m-0 text-sm font-medium text-slate-500">{formatTrainDateTimeRange(displayTrain.departureTime, displayTrain.arrivalTime)}</p>
          </div>

        </div>

        <div className="grid gap-3 md:grid-cols-3">
          <OrderMeta label={translate('booking.reference')} value={order.orderId} />
          <OrderMeta label={translate('booking.createdAt')} value={formatIsoDateTime(order.createdAt, translate('booking.notYet'))} />
          <OrderMeta label={translate('booking.paidAt')} value={formatIsoDateTime(order.paidAt, translate('booking.notYet'))} />
        </div>

        <ul className="grid gap-3 border-t border-slate-200 pt-4">
          {trainLineItems.map(orderLineItem => {
            const existingReviewForLineItem = findOrderItemReview(reviews, orderLineItem.orderItemId)
            return (
              <li key={orderLineItem.orderItemId} className="grid gap-3">
                <OrderLineItemDetails currentLanguage={currentLanguage} orderLineItem={orderLineItem} existingReview={existingReviewForLineItem} travelers={travelers} translate={translate} />
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
