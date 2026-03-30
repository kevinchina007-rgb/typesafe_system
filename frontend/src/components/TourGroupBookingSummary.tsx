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
              </div>
              <span className="tag-chip">{localizeTourGroupStatus(order.status, currentLanguage)}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
