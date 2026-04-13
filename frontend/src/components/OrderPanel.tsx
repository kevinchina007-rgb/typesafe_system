import { useState } from 'react'

import type { AppLanguage, OrderResponse, ReviewEligibilityResponse, ReviewResponse, TravelerResponse } from '../lib/mvp-types'
import { ReviewComposerDialog } from './ReviewComposerDialog'
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

// OrderPanel 的职责是把后端订单响应转成用户能读懂的订单视图。
// 这里不负责请求本身，只负责“订单数据如何展示”。
type OrderPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  orders: OrderResponse[]
  reviews: ReviewResponse[]
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onReloadOrders: () => Promise<void>
  onOpenPayment: (order: OrderResponse) => void
  onCancelOrder: (orderId: string) => Promise<void>
  onRequestRefund: (orderId: string, refundReason: string) => Promise<void>
  onLoadReviewEligibility: (orderItemId: string) => Promise<ReviewEligibilityResponse>
  onUploadReviewImage: (imageFile: File) => Promise<import('../lib/mvp-types').ContentImageResponse>
  onCreateReview: (payload: { orderId: string; orderItemId: string; rating: number; title: string; content: string; images: import('../lib/mvp-types').ContentImageResponse[] }) => Promise<void>
  onUpdateReview: (reviewId: string, payload: { rating: number; title: string; content: string; images: import('../lib/mvp-types').ContentImageResponse[] }) => Promise<void>
  onDeleteReview: (reviewId: string) => Promise<void>
}

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
  onLoadReviewEligibility,
  onUploadReviewImage,
  onCreateReview,
  onUpdateReview,
  onDeleteReview,
}: OrderPanelProps) {
  const [pendingReviewTarget, setPendingReviewTarget] = useState<{ orderId: string; orderItemId: string; title: string } | null>(null)
  const [pendingReviewEligibility, setPendingReviewEligibility] = useState<ReviewEligibilityResponse | null>(null)
  const [editingReview, setEditingReview] = useState<ReviewResponse | null>(null)

  async function openReviewDialog(orderId: string, orderItemId: string, title: string) {
    // 订单项与评价的关联主键是 orderItemId。
    // 已有评价时直接进入编辑模式，没有评价时先查资格。
    const existingReview = reviews.find(review => review.orderItemId === orderItemId)
    if (existingReview) {
      setEditingReview(existingReview)
      setPendingReviewEligibility(null)
      setPendingReviewTarget({ orderId, orderItemId, title })
      return
    }
    setPendingReviewTarget({ orderId, orderItemId, title })
    const eligibility = await onLoadReviewEligibility(orderItemId)
    setPendingReviewEligibility(eligibility)
  }

  function formatTravelerIdentity(travelerId: string): string {
    // 订单快照里保留的是 travelerId，展示时再映射成“姓名 + 证件后四位”。
    const matchedTraveler = travelers.find(traveler => traveler.travelerId === travelerId)
    if (!matchedTraveler) {
      return travelerId
    }
    const documentSuffix = matchedTraveler.documentNumber.slice(-4)
    return `${matchedTraveler.fullName} (${documentSuffix})`
  }

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
                      const existingReview = reviews.find(review => review.orderItemId === orderLineItem.orderItemId)
                      return (
                      <li key={orderLineItem.orderItemId}>
                        <div>
                          {/* 各资源类型共用同一张订单卡，但明细展示按资源类型分支。 */}
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
                              {orderLineItem.trainDetails.seatAssignments.length > 0 ? (
                                <p>
                                  {`${translate('booking.train.assignedSeats')}: ${orderLineItem.trainDetails.seatAssignments
                                    .map(
                                      seatAssignment =>
                                        `${formatTravelerIdentity(seatAssignment.travelerId)} ${translate('booking.train.carriageNo')}${seatAssignment.carriageNo} ${translate('booking.train.seatNo')}${seatAssignment.seatNo} (${seatAssignment.seatLabel})`,
                                    )
                                    .join(' | ')}`}
                                </p>
                              ) : null}
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
                              {(orderLineItem.attractionDetails.eligibilityRuleSummaries ?? []).length > 0 ? (
                                <p>
                                  {`${translate('booking.attraction.rules')}: ${(orderLineItem.attractionDetails.eligibilityRuleSummaries ?? []).join(' | ')}`}
                                </p>
                              ) : null}
                            </>
                          ) : null}
                          {orderLineItem.supplierReviewDecision?.reason ? (
                            <p>{orderLineItem.supplierReviewDecision.reason}</p>
                          ) : null}
                          {existingReview ? (
                            <p>{`${translate('reviews.alreadyWritten')}: ${existingReview.title}`}</p>
                          ) : null}
                        </div>
                        <div className="compact-action-block">
                          <span className="tag-chip">{`${orderLineItem.bookedAmount} ${orderLineItem.bookedCurrency}`}</span>
                          {!isGuestMode ? (
                            <button
                              type="button"
                              className="secondary-button"
                              disabled={isBusy}
                              onClick={() => void openReviewDialog(order.orderId, orderLineItem.orderItemId, orderLineItem.summaryLabel)}
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
                      <strong>
                        {(order.orderPayments ?? [])
                          .map(payment => `${localizePaymentMethod(payment.paymentMethod, currentLanguage)} ${payment.paymentAmount} ${payment.paymentCurrency}`)
                          .join(' | ')}
                      </strong>
                    </div>
                  ) : null}

                  {(order.orderRefunds ?? []).length > 0 ? (
                    <div className="compact-action-block">
                      <span className="detail-label">{translate('booking.section.refunds')}</span>
                      <strong>
                        {(order.orderRefunds ?? [])
                          .map(refund => `${refund.refundAmount} ${refund.refundCurrency} ${mapBackendStatusToProductLabel(refund.refundStatus, currentLanguage)}`)
                          .join(' | ')}
                      </strong>
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

      <ReviewComposerDialog
        isOpen={pendingReviewTarget !== null}
        isBusy={isBusy}
        eligibility={editingReview ? null : pendingReviewEligibility}
        mode={editingReview ? 'edit' : 'create'}
        initialValue={
          editingReview
            ? {
                rating: editingReview.rating,
                title: editingReview.title,
                content: editingReview.content,
                images: editingReview.images,
              }
            : null
        }
        title={pendingReviewTarget?.title ?? ''}
        translate={translate}
        onClose={() => {
          setPendingReviewTarget(null)
          setPendingReviewEligibility(null)
          setEditingReview(null)
        }}
        onUploadImage={onUploadReviewImage}
        onSubmit={async payload => {
          if (!pendingReviewTarget) {
            return
          }
          if (editingReview) {
            await onUpdateReview(editingReview.reviewId, payload)
          } else {
            await onCreateReview({
              orderId: pendingReviewTarget.orderId,
                orderItemId: pendingReviewTarget.orderItemId,
              rating: payload.rating,
              title: payload.title,
              content: payload.content,
              images: payload.images,
            })
          }
          setPendingReviewTarget(null)
          setPendingReviewEligibility(null)
          setEditingReview(null)
        }}
      />
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
