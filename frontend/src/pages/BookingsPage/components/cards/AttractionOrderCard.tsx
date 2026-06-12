// 本文件定义 BookingsPage 页面的卡片组件，负责单条数据摘要展示。

import type { OrderResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import { FlightTravelerBadges } from '@/pages/BookingsPage/components/shared/FlightTravelerBadges'
import { HotelInfoBlock } from '@/pages/BookingsPage/components/shared/HotelInfoBlock'
import { OrderItemFeedbackActions } from '@/pages/BookingsPage/components/shared/OrderItemFeedbackActions'
import type { OrderPanelProps } from '@/pages/BookingsPage/objects'
import {
  buildAttractionOrderDisplay,
  findOrderItemReview,
  isAttractionOrderLineItem,
  isOrderPaid,
  isOrderPayable,
  isOrderRefunded,
} from '@/pages/BookingsPage/functions'

export function AttractionOrderCard({
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
  const attractionLineItems = (order.orderLineItems ?? []).filter(orderLineItem => isAttractionOrderLineItem(orderLineItem))
  const attractionItem = attractionLineItems[0] ?? null
  const displayAttraction = attractionItem ? buildAttractionOrderDisplay(attractionItem) : null
  const existingReview = attractionItem ? findOrderItemReview(reviews, attractionItem.orderItemId) : null
  const isPayable = isOrderPayable(order.status)
  const isPaid = isOrderPaid(order.status)
  const isRefunded = isOrderRefunded(order.status)

  if (!displayAttraction || !attractionItem) {
    return <li className="border border-slate-200 bg-white p-5 text-slate-500 shadow-sm shadow-slate-200/40">{translate('bookings.empty')}</li>
  }

  const sessionLabel =
    displayAttraction.sessionName ||
    [displayAttraction.sessionStartsAt, displayAttraction.sessionEndsAt].filter(Boolean).join(' -> ') ||
    '未选择场次'
  const ruleLabel = displayAttraction.eligibilityRuleSummaries.length > 0 ? displayAttraction.eligibilityRuleSummaries.join(' / ') : '无额外规则'

  return (
    <li className="relative border border-slate-200 bg-white p-6 shadow-sm shadow-slate-200/40">
      <span className="absolute right-6 top-5 text-xs font-medium text-slate-400">{formatIsoDateTime(order.createdAt, translate('booking.notYet'))}</span>

      <div className="grid gap-6 pr-28">
        <div className="grid gap-5 lg:grid-cols-[1.25fr_0.9fr]">
          <div className="grid gap-3">
            <div className="flex flex-wrap items-center gap-3">
              <span className="inline-flex min-h-9 items-center justify-center border border-cyan-200 bg-cyan-50 px-3 text-sm font-black text-cyan-700">景点订单</span>
              <span className="text-sm font-semibold uppercase tracking-wide text-slate-500">{displayAttraction.ticketTypeName || '未填写票型'}</span>
            </div>

            <div className="grid gap-2">
              <h3 className="m-0 text-4xl font-black tracking-normal text-slate-950">{displayAttraction.attractionName || '未填写景点名称'}</h3>
              <p className="m-0 text-base font-medium text-slate-600">{displayAttraction.useDate || '未填写使用日期'}</p>
            </div>

            <div className="grid gap-3 md:grid-cols-2">
              <HotelInfoBlock label={translate('booking.attraction.ticketType')} value={displayAttraction.ticketTypeName || '未填写票型'} />
              <HotelInfoBlock label={translate('booking.attraction.rules')} value={ruleLabel} />
              <HotelInfoBlock label={translate('booking.attraction.useDate')} value={displayAttraction.useDate || '未填写使用日期'} />
              <HotelInfoBlock label="场次" value={sessionLabel} />
            </div>

            <FlightTravelerBadges travelerIds={displayAttraction.travelerIds} travelers={travelers} />
          </div>

          <div className="grid gap-4 border border-slate-200 bg-slate-50 p-4">
            <div className="grid gap-2">
              <span className="text-sm font-semibold text-slate-500">订单状态</span>
              <div className="flex flex-wrap items-center gap-3">
                <strong className="text-3xl font-black text-slate-950">{`${order.totalPrice} ${order.orderCurrency}`}</strong>
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
          {attractionLineItems.map(orderLineItem => {
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

      <div className="mt-5 flex flex-wrap items-center gap-3">
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
    </li>
  )
}
