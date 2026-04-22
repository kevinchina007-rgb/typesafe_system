import type { AppLanguage, OrderLineItemResponse, ReviewResponse, TravelerResponse } from '../../lib/mvp-types'
import {
  formatIsoDateTime,
  localizeBookingKind,
  localizeCabinClass,
  localizeReservationStatus,
  localizeSupplierReviewStatus,
  localizeTrainSeatClass,
} from '../../lib/view-models'
import { formatTravelerIdentity } from './orderViewModel'

type OrderLineItemDetailsProps = {
  currentLanguage: AppLanguage
  orderLineItem: OrderLineItemResponse
  existingReview: ReviewResponse | null
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
}

export function OrderLineItemDetails({
  currentLanguage,
  orderLineItem,
  existingReview,
  travelers,
  translate,
}: OrderLineItemDetailsProps) {
  return (
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
          {orderLineItem.trainDetails.seatAssignments.length > 0 ? (
            <p>
              {`${translate('booking.train.assignedSeats')}: ${orderLineItem.trainDetails.seatAssignments
                .map(
                  seatAssignment =>
                    `${formatTravelerIdentity(travelers, seatAssignment.travelerId)} ${translate('booking.train.carriageNo')}${seatAssignment.carriageNo} ${translate('booking.train.seatNo')}${seatAssignment.seatNo} (${seatAssignment.seatLabel})`,
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
          <p>{`${translate('booking.attraction.useDate')}: ${orderLineItem.attractionDetails.useDate}`}</p>
          <p>
            {`${translate('booking.attraction.unitPrice')}: ${orderLineItem.attractionDetails.unitPrice} ${orderLineItem.attractionDetails.currency} | ${translate('booking.attraction.totalPrice')}: ${orderLineItem.attractionDetails.totalPrice} ${orderLineItem.attractionDetails.currency}`}
          </p>
          {(orderLineItem.attractionDetails.eligibilityRuleSummaries ?? []).length > 0 ? (
            <p>{`${translate('booking.attraction.rules')}: ${(orderLineItem.attractionDetails.eligibilityRuleSummaries ?? []).join(' | ')}`}</p>
          ) : null}
        </>
      ) : null}

      {orderLineItem.supplierReviewDecision?.reason ? <p>{orderLineItem.supplierReviewDecision.reason}</p> : null}
      {existingReview ? <p>{`${translate('reviews.alreadyWritten')}: ${existingReview.title}`}</p> : null}
    </div>
  )
}
