import { formatIsoDateTime, localizeBookingKind, localizePaymentMethod, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import { OrderLineItemDetails } from '@/pages/BookingsPage/components/OrderLineItemDetails'
import { RefundActionForm } from '@/pages/BookingsPage/components/RefundActionForm'
import { buildOrderPaymentSummary, buildOrderRefundSummary, findOrderItemReview } from '@/pages/BookingsPage/components/orderViewModel'
import type { OrderPanelProps } from '@/pages/BookingsPage/components/orderViewModel'

export function OrderPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  orders,
  reviews,
  travelers,
  translate,
  onRequireLogin,
  onReloadOrders,
  onOpenPayment,
  onCancelOrder,
  onRequestRefund,
  onDeleteReview,
  onOpenFeedbackForReview,
  onStartReviewInFeedback,
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
        {isGuestMode ? (
          <div className="stack-form">
            <p className="empty-state">{translate('bookings.guest')}</p>
            <div className="action-row">
              <button type="button" onClick={onRequireLogin}>
                {translate('bookings.loginToPay')}
              </button>
            </div>
          </div>
        ) : null}
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
                    {(order.orderLineItems ?? []).map(orderLineItem => {
                      const existingReview = findOrderItemReview(reviews, orderLineItem.orderItemId)
                      return (
                        <li key={orderLineItem.orderItemId}>
                          <OrderLineItemDetails
                            currentLanguage={currentLanguage}
                            orderLineItem={orderLineItem}
                            existingReview={existingReview}
                            travelers={travelers}
                            translate={translate}
                          />
                          <div className="compact-action-block">
                            <span className="tag-chip">{`${orderLineItem.bookedAmount} ${orderLineItem.bookedCurrency}`}</span>
                            {!isGuestMode ? (
                              <button
                                type="button"
                                className="secondary-button"
                                disabled={isBusy}
                                onClick={() =>
                                  existingReview
                                    ? void onOpenFeedbackForReview(existingReview.reviewId)
                                    : void onStartReviewInFeedback({
                                        orderId: order.orderId,
                                        orderItemId: orderLineItem.orderItemId,
                                        title: orderLineItem.summaryLabel,
                                      })
                                }
                              >
                                {existingReview ? translate('reviews.viewOrEdit') : translate('reviews.write')}
                              </button>
                            ) : null}
                            {existingReview?.canDelete ? (
                              <button
                                type="button"
                                className="secondary-button"
                                disabled={isBusy}
                                onClick={() => void onDeleteReview(existingReview.reviewId)}
                              >
                                {translate('reviews.delete')}
                              </button>
                            ) : null}
                          </div>
                        </li>
                      )
                    })}
                  </ul>

                  {(order.orderPayments ?? []).length > 0 ? (
                    <div className="compact-action-block">
                      <span className="detail-label">{translate('booking.section.payments')}</span>
                      <strong>{buildOrderPaymentSummary(order, localizePaymentMethod, currentLanguage)}</strong>
                    </div>
                  ) : null}

                  {(order.orderRefunds ?? []).length > 0 ? (
                    <div className="compact-action-block">
                      <span className="detail-label">{translate('booking.section.refunds')}</span>
                      <strong>{buildOrderRefundSummary(order, currentLanguage, mapBackendStatusToProductLabel)}</strong>
                    </div>
                  ) : null}
                </div>

                <div className="manager-task-actions order-action-block">
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
