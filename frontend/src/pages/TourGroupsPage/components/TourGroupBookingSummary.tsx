// 本文件定义 TourGroupsPage 页面的页面组件。

import type { AppLanguage, OrderResponse } from '@/lib/mvp-types/index'
import { formatBookingSummary } from '@/lib/presenters/tour-group-presenter'
import { localizeTourGroupStatus } from '@/lib/presenters/view-models'

type TourGroupBookingSummaryProps = {
  currentLanguage: AppLanguage
  bookings: OrderResponse[]
  translate: (translationKey: string) => string
}

export function TourGroupBookingSummary({ currentLanguage, bookings, translate }: TourGroupBookingSummaryProps) {
  return (
    <section className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
      <div className="text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('tourGroups.bookingSectionEyebrow')}</p>
          <h3>{translate('tourGroups.bookings')}</h3>
        </div>
      </div>

      {bookings.length === 0 ? (
        <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.noBookings')}</p>
      ) : (
        <ul className="grid gap-3">
          {bookings.map(order => (
            <li key={order.orderId}>
              <div>
                <strong>{order.orderId}</strong>
                <p>{formatBookingSummary(order, currentLanguage, translate)}</p>
                <p>{`${translate('tourGroups.paymentStatusSummary')}: ${order.orderPayments.map(payment => payment.paymentStatus).join(', ') || translate('tourGroups.paymentPending')}`}</p>
                <p>{`${translate('tourGroups.supplierStatusSummary')}: ${order.orderLineItems.map(item => item.supplierReviewStatus).join(', ')}`}</p>
                {order.orderRefunds.length > 0 ? (
                  <p>{`${translate('tourGroups.refundStatusSummary')}: ${order.orderRefunds.map(refund => refund.refundStatus).join(', ')}`}</p>
                ) : null}
              </div>
              <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{localizeTourGroupStatus(order.status, currentLanguage)}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
