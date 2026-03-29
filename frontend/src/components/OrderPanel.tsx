import type { AppLanguage, OrderResponse } from '../lib/mvp-types'
import {
  formatIsoDateTime,
  localizeBookingKind,
  localizeCabinClass,
  localizePaymentMethod,
  localizeReservationStatus,
  localizeSupplierReviewStatus,
  localizeTrainSeatClass,
  mapBackendStatusToProductLabel,
} from '../lib/view-models'

type OrderPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  orders: OrderResponse[]
  translate: (translationKey: string) => string
  onReloadOrders: () => Promise<void>
  onOpenPayment: (order: OrderResponse) => void
  onCancelOrder: (orderId: string) => Promise<void>
  onRequestRefund: (orderId: string, refundReason: string) => Promise<void>
}

export function OrderPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  orders,
  translate,
  onReloadOrders,
  onOpenPayment,
  onCancelOrder,
  onRequestRefund,
}: OrderPanelProps) {
  return (
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.bookings')}</p>
          <h2>{translate('bookings.title')}</h2>
        </div>
        <button type="button" disabled={isGuestMode || isBusy} className="secondary-button" onClick={() => void onReloadOrders()}>
          {translate('bookings.refresh')}
        </button>
      </div>

      <p className="hero-copy">{translate('bookings.description')}</p>

      <div className="list-surface">
        {isGuestMode ? <p className="empty-state">{translate('bookings.guest')}</p> : null}
        {!isGuestMode && orders.length === 0 ? <p className="empty-state">{translate('bookings.empty')}</p> : null}

        {orders.length > 0 ? (
          <ul className="entity-list">
            {orders.map(order => (
              <li key={order.orderId}>
                <div className="order-card-content">
                  <div className="detail-grid">
                    <div>
                      <span className="detail-label">{translate('booking.reference')}</span>
                      <strong>{order.orderId}</strong>
                    </div>
                    <div>
                      <span className="detail-label">{translate('booking.type')}</span>
                      <strong>{localizeBookingKind(order.orderType, currentLanguage)}</strong>
                    </div>
                    <div>
                      <span className="detail-label">{translate('booking.status')}</span>
                      <strong>{mapBackendStatusToProductLabel(order.status, currentLanguage)}</strong>
                    </div>
                    <div>
                      <span className="detail-label">{translate('booking.totalPrice')}</span>
                      <strong>{`${order.totalPrice} ${order.orderCurrency}`}</strong>
                    </div>
                    <div>
                      <span className="detail-label">{translate('booking.createdAt')}</span>
                      <strong>{formatIsoDateTime(order.createdAt, translate('booking.notYet'))}</strong>
                    </div>
                    <div>
                      <span className="detail-label">{translate('booking.paidAt')}</span>
                      <strong>{formatIsoDateTime(order.paidAt, translate('booking.notYet'))}</strong>
                    </div>
                  </div>

                  <ul className="entity-list">
                    {order.orderLineItems.map(orderLineItem => (
                      <li key={orderLineItem.orderItemId}>
                        <div>
                          <strong>{orderLineItem.summaryLabel}</strong>
                          <p>{`${localizeBookingKind(orderLineItem.orderItemKind, currentLanguage)} | ${localizeSupplierReviewStatus(orderLineItem.supplierReviewStatus, currentLanguage)}`}</p>
                          {orderLineItem.flightDetails ? (
                            <>
                              <p>
                                {`${translate('booking.flight.cabin')}: ${localizeCabinClass(orderLineItem.flightDetails.cabinClass, currentLanguage)} | ${translate('booking.flight.travelers')}: ${orderLineItem.flightDetails.travelerIds.length}`}
                              </p>
                              <p>
                                {`${translate('booking.flight.departureTime')}: ${formatIsoDateTime(orderLineItem.flightDetails.departureTime, translate('booking.notYet'))} | ${translate('booking.flight.arrivalTime')}: ${formatIsoDateTime(orderLineItem.flightDetails.arrivalTime, translate('booking.notYet'))}`}
                              </p>
                              {orderLineItem.flightDetails.reservationStatus ? (
                                <p>
                                  {`${translate('booking.flight.reservation')}: ${localizeReservationStatus(orderLineItem.flightDetails.reservationStatus, currentLanguage)}`}
                                  {orderLineItem.flightDetails.reservationExpiresAt
                                    ? ` | ${translate('booking.flight.reservationExpiresAt')}: ${formatIsoDateTime(orderLineItem.flightDetails.reservationExpiresAt, translate('booking.notYet'))}`
                                    : ''}
                                </p>
                              ) : null}
                            </>
                          ) : null}
                          {orderLineItem.hotelDetails ? (
                            <>
                              <p>
                                {`${translate('booking.hotel.roomType')}: ${orderLineItem.hotelDetails.roomTypeName} | ${translate('booking.hotel.guests')}: ${orderLineItem.hotelDetails.guestTravelerIds.length} | ${translate('booking.hotel.roomCount')}: ${orderLineItem.hotelDetails.roomCount}`}
                              </p>
                              <p>
                                {`${translate('booking.hotel.stay')}: ${orderLineItem.hotelDetails.checkInDate} -> ${orderLineItem.hotelDetails.checkOutDate}`}
                              </p>
                              {orderLineItem.hotelDetails.reservationStatus ? (
                                <p>
                                  {`${translate('booking.hotel.reservation')}: ${localizeReservationStatus(orderLineItem.hotelDetails.reservationStatus, currentLanguage)}`}
                                  {orderLineItem.hotelDetails.reservationExpiresAt
                                    ? ` | ${translate('booking.hotel.reservationExpiresAt')}: ${formatIsoDateTime(orderLineItem.hotelDetails.reservationExpiresAt, translate('booking.notYet'))}`
                                    : ''}
                                </p>
                              ) : null}
                              <p>
                                {`${translate('booking.hotel.unitPrice')}: ${orderLineItem.hotelDetails.unitPrice} ${orderLineItem.hotelDetails.currency} | ${translate('booking.hotel.totalPrice')}: ${orderLineItem.hotelDetails.totalPrice} ${orderLineItem.hotelDetails.currency}`}
                              </p>
                            </>
                          ) : null}
                          {orderLineItem.trainDetails ? (
                            <>
                              <p>
                                {`${translate('booking.train.route')}: ${orderLineItem.trainDetails.fromStationName} (${orderLineItem.trainDetails.fromStationCode}) -> ${orderLineItem.trainDetails.toStationName} (${orderLineItem.trainDetails.toStationCode})`}
                              </p>
                              <p>
                                {`${translate('booking.train.seatClass')}: ${localizeTrainSeatClass(orderLineItem.trainDetails.seatClass, currentLanguage)} | ${translate('booking.train.travelers')}: ${orderLineItem.trainDetails.travelerIds.length}`}
                              </p>
                              <p>
                                {`${translate('booking.train.departureTime')}: ${formatIsoDateTime(orderLineItem.trainDetails.departureTime, translate('booking.notYet'))} | ${translate('booking.train.arrivalTime')}: ${formatIsoDateTime(orderLineItem.trainDetails.arrivalTime, translate('booking.notYet'))}`}
                              </p>
                              {orderLineItem.trainDetails.reservationStatus ? (
                                <p>
                                  {`${translate('booking.train.reservation')}: ${localizeReservationStatus(orderLineItem.trainDetails.reservationStatus, currentLanguage)}`}
                                  {orderLineItem.trainDetails.reservationExpiresAt
                                    ? ` | ${translate('booking.train.reservationExpiresAt')}: ${formatIsoDateTime(orderLineItem.trainDetails.reservationExpiresAt, translate('booking.notYet'))}`
                                    : ''}
                                </p>
                              ) : null}
                              <p>
                                {`${translate('booking.train.unitPrice')}: ${orderLineItem.trainDetails.unitPrice} ${orderLineItem.trainDetails.currency} | ${translate('booking.train.totalPrice')}: ${orderLineItem.trainDetails.totalPrice} ${orderLineItem.trainDetails.currency}`}
                              </p>
                            </>
                          ) : null}
                          {orderLineItem.attractionDetails ? (
                            <>
                              <p>
                                {`${translate('booking.attraction.ticketType')}: ${orderLineItem.attractionDetails.ticketTypeName} | ${translate('booking.attraction.travelers')}: ${orderLineItem.attractionDetails.travelerIds.length}`}
                              </p>
                              <p>
                                {`${translate('booking.attraction.useDate')}: ${orderLineItem.attractionDetails.useDate}`}
                              </p>
                              <p>
                                {`${translate('booking.attraction.unitPrice')}: ${orderLineItem.attractionDetails.unitPrice} ${orderLineItem.attractionDetails.currency} | ${translate('booking.attraction.totalPrice')}: ${orderLineItem.attractionDetails.totalPrice} ${orderLineItem.attractionDetails.currency}`}
                              </p>
                              {orderLineItem.attractionDetails.eligibilityRuleSummaries.length > 0 ? (
                                <p>
                                  {`${translate('booking.attraction.rules')}: ${orderLineItem.attractionDetails.eligibilityRuleSummaries.join(' | ')}`}
                                </p>
                              ) : null}
                            </>
                          ) : null}
                          {orderLineItem.supplierReviewDecision?.reason ? (
                            <p>{orderLineItem.supplierReviewDecision.reason}</p>
                          ) : null}
                        </div>
                        <span className="tag-chip">{`${orderLineItem.bookedAmount} ${orderLineItem.bookedCurrency}`}</span>
                      </li>
                    ))}
                  </ul>

                  {order.orderPayments.length > 0 ? (
                    <div className="compact-action-block">
                      <span className="detail-label">{translate('booking.section.payments')}</span>
                      <strong>
                        {order.orderPayments
                          .map(payment => `${localizePaymentMethod(payment.paymentMethod, currentLanguage)} ${payment.paymentAmount} ${payment.paymentCurrency}`)
                          .join(' | ')}
                      </strong>
                    </div>
                  ) : null}

                  {order.orderRefunds.length > 0 ? (
                    <div className="compact-action-block">
                      <span className="detail-label">{translate('booking.section.refunds')}</span>
                      <strong>
                        {order.orderRefunds
                          .map(refund => `${refund.refundAmount} ${refund.refundCurrency} ${mapBackendStatusToProductLabel(refund.refundStatus, currentLanguage)}`)
                          .join(' | ')}
                      </strong>
                    </div>
                  ) : null}
                </div>

                <div className="manager-task-actions">
                  {order.status === 'PendingPayment' ? (
                    <>
                      <button type="button" disabled={isBusy} onClick={() => onOpenPayment(order)}>
                        {translate('bookings.pay')}
                      </button>
                      <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void onCancelOrder(order.orderId)}>
                        {translate('bookings.cancel')}
                      </button>
                    </>
                  ) : null}

                  {order.status === 'Paid' || order.status === 'Booked' ? (
                    <RefundActionForm
                      disabled={isBusy}
                      translate={translate}
                      onSubmit={refundReason => onRequestRefund(order.orderId, refundReason)}
                    />
                  ) : null}
                </div>
              </li>
            ))}
          </ul>
        ) : null}
      </div>
    </section>
  )
}

function RefundActionForm({
  disabled,
  translate,
  onSubmit,
}: {
  disabled: boolean
  translate: (translationKey: string) => string
  onSubmit: (refundReason: string) => Promise<void>
}) {
  return (
    <form
      className="inline-form"
      onSubmit={async event => {
        event.preventDefault()
        const formData = new FormData(event.currentTarget)
        await onSubmit(String(formData.get('refundReason') ?? ''))
        event.currentTarget.reset()
      }}
    >
      <label>
        {translate('bookings.refund.reason')}
        <input name="refundReason" placeholder={translate('bookings.refund.reasonPlaceholder')} disabled={disabled} />
      </label>
      <button type="submit" disabled={disabled}>
        {translate('bookings.requestRefund')}
      </button>
    </form>
  )
}
