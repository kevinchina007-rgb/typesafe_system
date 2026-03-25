import type { AppLanguage, OrderResponse, TravelerResponse } from '../lib/mvp-types'
import {
  formatIsoDateTime,
  localizeBookingKind,
  localizePaymentMethod,
  mapBackendStatusToProductLabel,
} from '../lib/view-models'

type OrderPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  booking: OrderResponse | null
  translate: (translationKey: string) => string
  onCreateBooking: (payload: {
    travelerId: string
    orderCurrency: string
    itemKind: string
    providerId: string
    providerLabel: string
    productId: string
    referenceCode: string
    variantLabel: string
    originCode: string
    destinationCode: string
    periodStart: string
    periodEnd: string
    quantity: number
    bookedAmount: string
  }) => Promise<void>
  onReloadBooking: () => Promise<void>
  onSubmitBooking: () => Promise<void>
  onAuthorizePayment: (payload: {
    paymentAmount: string
    paymentCurrency: string
    paymentMethod: string
  }) => Promise<void>
  onCapturePayment: (paymentId: string) => Promise<void>
  onCancelBooking: () => Promise<void>
  onRequestRefund: (payload: {
    refundAmount: string
    refundCurrency: string
    refundReason: string
  }) => Promise<void>
  onApproveRefund: (refundId: string) => Promise<void>
  onSettleRefund: (refundId: string) => Promise<void>
}

export function OrderPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  booking,
  translate,
  onCreateBooking,
  onReloadBooking,
  onSubmitBooking,
  onAuthorizePayment,
  onCapturePayment,
  onCancelBooking,
  onRequestRefund,
  onApproveRefund,
  onSettleRefund,
}: OrderPanelProps) {
  return (
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.bookings')}</p>
          <h2>{translate('bookings.title')}</h2>
        </div>
        <button
          type="button"
          disabled={!booking || isGuestMode || isBusy}
          className="secondary-button"
          onClick={() => void onReloadBooking()}
        >
          {translate('bookings.refresh')}
        </button>
      </div>

      <p className="hero-copy">{translate('bookings.description')}</p>

      <form
        className="stack-form panel-card"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          await onCreateBooking({
            travelerId: String(formData.get('travelerId') ?? ''),
            orderCurrency: String(formData.get('orderCurrency') ?? 'CNY'),
            itemKind: String(formData.get('itemKind') ?? 'hotel'),
            providerId: String(formData.get('providerId') ?? ''),
            providerLabel: String(formData.get('providerLabel') ?? ''),
            productId: String(formData.get('productId') ?? ''),
            referenceCode: String(formData.get('referenceCode') ?? ''),
            variantLabel: String(formData.get('variantLabel') ?? ''),
            originCode: String(formData.get('originCode') ?? ''),
            destinationCode: String(formData.get('destinationCode') ?? ''),
            periodStart: String(formData.get('periodStart') ?? ''),
            periodEnd: String(formData.get('periodEnd') ?? ''),
            quantity: Number(formData.get('quantity') ?? 1),
            bookedAmount: String(formData.get('bookedAmount') ?? ''),
          })
        }}
      >
        <div className="three-column-grid">
          <label>
            {translate('bookings.traveler')}
            <select name="travelerId" disabled={isGuestMode || travelers.length === 0 || isBusy}>
              {travelers.map(traveler => (
                <option key={traveler.travelerId} value={traveler.travelerId}>
                  {traveler.fullName}
                </option>
              ))}
            </select>
          </label>
          <label>
            {translate('bookings.currency')}
            <select name="orderCurrency" defaultValue="CNY" disabled={isGuestMode || isBusy}>
              <option value="CNY">CNY</option>
              <option value="USD">USD</option>
              <option value="EUR">EUR</option>
            </select>
          </label>
          <label>
            {translate('bookings.kind')}
            <select name="itemKind" defaultValue="hotel" disabled={isGuestMode || isBusy}>
              <option value="hotel">{translate('bookings.kind.hotel')}</option>
              <option value="flight">{translate('bookings.kind.flight')}</option>
            </select>
          </label>
        </div>

        <div className="three-column-grid">
          <label>
            {translate('bookings.providerId')}
            <input name="providerId" defaultValue="provider-001" required disabled={isGuestMode || isBusy} />
          </label>
          <label>
            {translate('bookings.providerName')}
            <input name="providerLabel" defaultValue="West Lake Retreat" required disabled={isGuestMode || isBusy} />
          </label>
          <label>
            {translate('bookings.productId')}
            <input name="productId" defaultValue="product-001" required disabled={isGuestMode || isBusy} />
          </label>
        </div>

        <div className="three-column-grid">
          <label>
            {translate('bookings.reference')}
            <input name="referenceCode" defaultValue="MU5123" required disabled={isGuestMode || isBusy} />
          </label>
          <label>
            {translate('bookings.variant')}
            <input name="variantLabel" defaultValue="DeluxeRoom" required disabled={isGuestMode || isBusy} />
          </label>
          <label>
            {translate('bookings.price')}
            <input name="bookedAmount" defaultValue="1888" required disabled={isGuestMode || isBusy} />
          </label>
        </div>

        <div className="three-column-grid">
          <label>
            {translate('bookings.origin')}
            <input name="originCode" defaultValue="SHA" disabled={isGuestMode || isBusy} />
          </label>
          <label>
            {translate('bookings.destination')}
            <input name="destinationCode" defaultValue="HGH" disabled={isGuestMode || isBusy} />
          </label>
          <label>
            {translate('bookings.quantity')}
            <input name="quantity" type="number" min="1" defaultValue="1" required disabled={isGuestMode || isBusy} />
          </label>
        </div>

        <div className="two-column-grid">
          <label>
            {translate('bookings.start')}
            <input name="periodStart" defaultValue="2026-04-01" required disabled={isGuestMode || isBusy} />
          </label>
          <label>
            {translate('bookings.end')}
            <input name="periodEnd" defaultValue="2026-04-03" required disabled={isGuestMode || isBusy} />
          </label>
        </div>

        <button type="submit" disabled={isGuestMode || travelers.length === 0 || isBusy}>
          {translate('bookings.create')}
        </button>
      </form>

      <div className="action-cluster">
        <button type="button" disabled={!booking || isGuestMode || isBusy} onClick={() => void onSubmitBooking()}>
          {translate('bookings.continuePayment')}
        </button>
        <button type="button" className="secondary-button" disabled={!booking || isGuestMode || isBusy} onClick={() => void onCancelBooking()}>
          {translate('bookings.cancel')}
        </button>
      </div>

      <form
        className="inline-form"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          await onAuthorizePayment({
            paymentAmount: String(formData.get('paymentAmount') ?? ''),
            paymentCurrency: String(formData.get('paymentCurrency') ?? 'CNY'),
            paymentMethod: String(formData.get('paymentMethod') ?? 'card'),
          })
        }}
      >
        <input name="paymentAmount" defaultValue={booking?.totalPrice ?? '1888'} disabled={!booking || isGuestMode || isBusy} />
        <select name="paymentCurrency" defaultValue={booking?.orderCurrency ?? 'CNY'} disabled={!booking || isGuestMode || isBusy}>
          <option value="CNY">CNY</option>
          <option value="USD">USD</option>
          <option value="EUR">EUR</option>
        </select>
        <select name="paymentMethod" defaultValue="card" disabled={!booking || isGuestMode || isBusy}>
          <option value="card">{translate('bookings.payment.method.card')}</option>
          <option value="bank-transfer">{translate('bookings.payment.method.bank-transfer')}</option>
          <option value="wallet">{translate('bookings.payment.method.wallet')}</option>
        </select>
        <button type="submit" disabled={!booking || isGuestMode || isBusy}>
          {translate('bookings.pay')}
        </button>
      </form>

      <form
        className="inline-form"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          await onRequestRefund({
            refundAmount: String(formData.get('refundAmount') ?? ''),
            refundCurrency: String(formData.get('refundCurrency') ?? 'CNY'),
            refundReason: String(formData.get('refundReason') ?? ''),
          })
        }}
      >
        <input name="refundAmount" defaultValue={booking?.totalCapturedAmount ?? '100'} disabled={!booking || isGuestMode || isBusy} />
        <select name="refundCurrency" defaultValue={booking?.orderCurrency ?? 'CNY'} disabled={!booking || isGuestMode || isBusy}>
          <option value="CNY">CNY</option>
          <option value="USD">USD</option>
          <option value="EUR">EUR</option>
        </select>
        <input
          name="refundReason"
          defaultValue={translate('bookings.refund.reasonPlaceholder')}
          disabled={!booking || isGuestMode || isBusy}
        />
        <button type="submit" disabled={!booking || isGuestMode || isBusy}>
          {translate('bookings.requestRefund')}
        </button>
      </form>

      <div className="list-surface">
        {isGuestMode ? <p className="empty-state">{translate('bookings.guest')}</p> : null}
        {booking ? (
          <>
            <div className="detail-grid">
              <div>
                <span className="detail-label">{translate('booking.reference')}</span>
                <strong>{booking.orderId}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('booking.type')}</span>
                <strong>{localizeBookingKind(booking.orderType, currentLanguage)}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('booking.status')}</span>
                <strong>{mapBackendStatusToProductLabel(booking.status, currentLanguage)}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('booking.totalPrice')}</span>
                <strong>{`${booking.totalPrice} ${booking.orderCurrency}`}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('booking.createdAt')}</span>
                <strong>{formatIsoDateTime(booking.createdAt, translate('booking.notYet'))}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('booking.paidAt')}</span>
                <strong>{formatIsoDateTime(booking.paidAt, translate('booking.notYet'))}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('booking.confirmedAt')}</span>
                <strong>{formatIsoDateTime(booking.confirmedAt, translate('booking.notYet'))}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('booking.cancelledAt')}</span>
                <strong>{formatIsoDateTime(booking.cancelledAt, translate('booking.notYet'))}</strong>
              </div>
            </div>

            <h3>{translate('booking.section.items')}</h3>
            <ul className="entity-list">
              {booking.orderLineItems.map(orderLineItem => (
                <li key={orderLineItem.orderItemId}>
                  <div>
                    <strong>{orderLineItem.summaryLabel}</strong>
                    <p>{`${localizeBookingKind(orderLineItem.orderItemKind, currentLanguage)} · ${mapBackendStatusToProductLabel(orderLineItem.orderItemStatus, currentLanguage)}`}</p>
                  </div>
                  <span className="tag-chip">{`${orderLineItem.bookedAmount} ${orderLineItem.bookedCurrency}`}</span>
                </li>
              ))}
            </ul>

            <h3>{translate('booking.section.payments')}</h3>
            {booking.orderPayments.length > 0 ? (
              <ul className="entity-list">
                {booking.orderPayments.map(payment => (
                  <li key={payment.paymentId}>
                    <div>
                      <strong>{localizePaymentMethod(payment.paymentMethod, currentLanguage)}</strong>
                      <p>{mapBackendStatusToProductLabel(payment.paymentStatus, currentLanguage)}</p>
                    </div>
                    <div className="compact-action-block">
                      <span className="tag-chip">{`${payment.paymentAmount} ${payment.paymentCurrency}`}</span>
                      {payment.paymentStatus === 'Authorized' ? (
                        <button type="button" disabled={isGuestMode || isBusy} onClick={() => void onCapturePayment(payment.paymentId)}>
                          {translate('bookings.confirmPayment')}
                        </button>
                      ) : null}
                    </div>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="empty-state">{translate('booking.notYet')}</p>
            )}

            <h3>{translate('booking.section.refunds')}</h3>
            {booking.orderRefunds.length > 0 ? (
              <ul className="entity-list">
                {booking.orderRefunds.map(refund => (
                  <li key={refund.refundId}>
                    <div>
                      <strong>{refund.refundReason}</strong>
                      <p>{mapBackendStatusToProductLabel(refund.refundStatus, currentLanguage)}</p>
                    </div>
                    <div className="compact-action-block">
                      <span className="tag-chip">{`${refund.refundAmount} ${refund.refundCurrency}`}</span>
                      {refund.refundStatus === 'Requested' ? (
                        <button type="button" disabled={isGuestMode || isBusy} onClick={() => void onApproveRefund(refund.refundId)}>
                          {translate('bookings.approveRefund')}
                        </button>
                      ) : null}
                      {refund.refundStatus === 'Approved' ? (
                        <button type="button" disabled={isGuestMode || isBusy} onClick={() => void onSettleRefund(refund.refundId)}>
                          {translate('bookings.settleRefund')}
                        </button>
                      ) : null}
                    </div>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="empty-state">{translate('booking.notYet')}</p>
            )}
          </>
        ) : (
          <p className="empty-state">{translate('bookings.empty')}</p>
        )}
      </div>
    </section>
  )
}
