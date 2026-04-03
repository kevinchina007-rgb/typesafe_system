import type { AppLanguage, OrderResponse } from '../lib/mvp-types'
import { formatBookingSummary } from '../lib/tour-group-presenter'
import { localizeTourGroupStatus } from '../lib/view-models'

type TourGroupBookingSummaryProps = {
  currentLanguage: AppLanguage
  bookings: OrderResponse[]
  translate: (translationKey: string) => string
}

export function TourGroupBookingSummary({ currentLanguage, bookings, translate }: TourGroupBookingSummaryProps) {
  return (
    <section className="list-surface">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('tourGroups.bookingSectionEyebrow')}</p>
          <h3>{translate('tourGroups.bookings')}</h3>
        </div>
      </div>

      {bookings.length === 0 ? (
        <p className="empty-state">{translate('tourGroups.noBookings')}</p>
      ) : (
        <ul className="entity-list">
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
              <span className="tag-chip">{localizeTourGroupStatus(order.status, currentLanguage)}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
