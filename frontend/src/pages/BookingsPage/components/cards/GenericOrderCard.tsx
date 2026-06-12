// 本文件定义 BookingsPage 页面的卡片组件，负责单条数据摘要展示。

import type { OrderResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, localizeBookingKind, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import { OrderLineItemDetails } from '@/pages/BookingsPage/components/OrderLineItemDetails'
import { OrderItemFeedbackActions } from '@/pages/BookingsPage/components/shared/OrderItemFeedbackActions'
import { OrderMeta } from '@/pages/BookingsPage/components/shared/OrderMeta'
import { OrderPaymentActions } from '@/pages/BookingsPage/components/shared/OrderPaymentActions'
import type { OrderPanelProps } from '@/pages/BookingsPage/objects'
import { buildOrderPaymentSummary, buildOrderRefundSummary, findOrderItemReview, isAttractionOrderLineItem } from '@/pages/BookingsPage/functions'

export function GenericOrderCard({
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
  const visibleLineItems = (order.orderLineItems ?? []).filter(orderLineItem => isAttractionOrderLineItem(orderLineItem))

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
          {visibleLineItems.map(orderLineItem => {
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

        {(order.orderPayments ?? []).length > 0 ? (
          <div className="flex flex-wrap items-center gap-3">
            <span className="text-sm font-medium text-slate-500">{translate('booking.section.payments')}</span>
            <strong>{buildOrderPaymentSummary(order, currentLanguage)}</strong>
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
