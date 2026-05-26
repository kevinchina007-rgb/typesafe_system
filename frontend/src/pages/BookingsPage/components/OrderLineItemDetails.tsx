import type { AppLanguage, OrderLineItemResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, localizeBookingKind, localizeCabinClass, localizeReservationStatus, localizeSupplierReviewStatus, localizeTrainSeatClass } from '@/lib/presenters/view-models'
import { formatTravelerIdentity } from '@/pages/BookingsPage/components/orderViewModel'

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
  const snapshotDetails = parseFlightSnapshot(orderLineItem.summaryLabel)
  const hasStructuredDetails = !!(
    orderLineItem.flightDetails ||
    orderLineItem.hotelDetails ||
    orderLineItem.trainDetails ||
    orderLineItem.attractionDetails
  )

  return (
    <div className="grid gap-2 text-base text-slate-700">
      <strong className="text-xl font-bold text-slate-950">{formatOrderLineItemTitle(orderLineItem, snapshotDetails, currentLanguage)}</strong>
      <p className="m-0">{`${localizeBookingKind(orderLineItem.orderItemKind, currentLanguage)} | ${localizeSupplierReviewStatus(orderLineItem.supplierReviewStatus, currentLanguage)}`}</p>

      {!hasStructuredDetails && snapshotDetails ? (
        <>
          {snapshotDetails.flightId ? <p className="m-0">{`${translate('booking.flight.id')}: ${snapshotDetails.flightId}`}</p> : null}
          {snapshotDetails.cabinClass ? (
            <p className="m-0">{`${translate('booking.flight.cabin')}: ${localizeCabinClass(snapshotDetails.cabinClass, currentLanguage)}`}</p>
          ) : null}
          {snapshotDetails.travelerIds.length > 0 ? (
            <p className="m-0">
              {`${translate('booking.flight.travelers')}: ${snapshotDetails.travelerIds
                .map(travelerId => formatTravelerIdentity(travelers, travelerId))
                .join('、')}`}
            </p>
          ) : null}
        </>
      ) : null}

      {orderLineItem.flightDetails ? (
        <>
          <p className="m-0">
            {`${translate('booking.flight.cabin')}: ${localizeCabinClass(orderLineItem.flightDetails.cabinClass, currentLanguage)} | ${translate('booking.flight.travelers')}: ${orderLineItem.flightDetails.travelerIds.length}`}
          </p>
          <p className="m-0">
            {`${translate('booking.flight.departureTime')}: ${formatIsoDateTime(orderLineItem.flightDetails.departureTime, translate('booking.notYet'))} | ${translate('booking.flight.arrivalTime')}: ${formatIsoDateTime(orderLineItem.flightDetails.arrivalTime, translate('booking.notYet'))}`}
          </p>
          {orderLineItem.flightDetails.reservationStatus ? (
            <p className="m-0">
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
          <p className="m-0">
            {`${translate('booking.hotel.roomType')}: ${orderLineItem.hotelDetails.roomTypeName} | ${translate('booking.hotel.guests')}: ${orderLineItem.hotelDetails.guestTravelerIds.length} | ${translate('booking.hotel.roomCount')}: ${orderLineItem.hotelDetails.roomCount}`}
          </p>
          <p className="m-0">
            {`${translate('booking.hotel.stay')}: ${orderLineItem.hotelDetails.checkInDate} -> ${orderLineItem.hotelDetails.checkOutDate}`}
          </p>
          {orderLineItem.hotelDetails.reservationStatus ? (
            <p className="m-0">
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
            <p className="m-0">
              {`${translate('booking.train.assignedSeats')}: ${orderLineItem.trainDetails.seatAssignments
                .map(
                  seatAssignment =>
                    `${formatTravelerIdentity(travelers, seatAssignment.travelerId)} ${translate('booking.train.carriageNo')}${seatAssignment.carriageNo} ${translate('booking.train.seatNo')}${seatAssignment.seatNo} (${seatAssignment.seatLabel})`,
                )
                .join(' | ')}`}
            </p>
          ) : null}
          {orderLineItem.trainDetails.reservationStatus ? (
            <p className="m-0">
              {`${translate('booking.train.reservation')}: ${localizeReservationStatus(orderLineItem.trainDetails.reservationStatus, currentLanguage)}`}
              {orderLineItem.trainDetails.reservationExpiresAt
                ? ` | ${translate('booking.train.reservationExpiresAt')}: ${formatIsoDateTime(orderLineItem.trainDetails.reservationExpiresAt, translate('booking.notYet'))}`
                : ''}
            </p>
          ) : null}
          <p className="m-0">
            {`${translate('booking.train.unitPrice')}: ${orderLineItem.trainDetails.unitPrice} ${orderLineItem.trainDetails.currency} | ${translate('booking.train.totalPrice')}: ${orderLineItem.trainDetails.totalPrice} ${orderLineItem.trainDetails.currency}`}
          </p>
        </>
      ) : null}

      {orderLineItem.attractionDetails ? (
        <>
          <p className="m-0">
            {`${translate('booking.attraction.ticketType')}: ${orderLineItem.attractionDetails.ticketTypeName} | ${translate('booking.attraction.travelers')}: ${orderLineItem.attractionDetails.travelerIds.length}`}
          </p>
          <p className="m-0">{`${translate('booking.attraction.useDate')}: ${orderLineItem.attractionDetails.useDate}`}</p>
          <p className="m-0">
            {`${translate('booking.attraction.unitPrice')}: ${orderLineItem.attractionDetails.unitPrice} ${orderLineItem.attractionDetails.currency} | ${translate('booking.attraction.totalPrice')}: ${orderLineItem.attractionDetails.totalPrice} ${orderLineItem.attractionDetails.currency}`}
          </p>
          {(orderLineItem.attractionDetails.eligibilityRuleSummaries ?? []).length > 0 ? (
            <p className="m-0">{`${translate('booking.attraction.rules')}: ${(orderLineItem.attractionDetails.eligibilityRuleSummaries ?? []).join(' | ')}`}</p>
          ) : null}
        </>
      ) : null}

      {orderLineItem.supplierReviewDecision?.reason ? <p className="m-0">{orderLineItem.supplierReviewDecision.reason}</p> : null}
      {existingReview ? <p className="m-0">{`${translate('reviews.alreadyWritten')}: ${existingReview.title}`}</p> : null}
    </div>
  )
}

type FlightSnapshotSummary = {
  airlineName?: string
  flightNumber?: string
  flightId?: string
  cabinClass?: string
  travelerIds: string[]
}

function parseFlightSnapshot(summaryLabel: string): FlightSnapshotSummary | null {
  if (!summaryLabel.trim().startsWith('{')) {
    return null
  }

  try {
    const parsed = JSON.parse(summaryLabel) as {
      airlineName?: unknown
      flightNumber?: unknown
      flightId?: unknown
      cabinClass?: unknown
      travelerIds?: unknown
    }
    return {
      airlineName: typeof parsed.airlineName === 'string' ? parsed.airlineName : undefined,
      flightNumber: typeof parsed.flightNumber === 'string' ? parsed.flightNumber : undefined,
      flightId: typeof parsed.flightId === 'string' ? parsed.flightId : undefined,
      cabinClass: typeof parsed.cabinClass === 'string' ? parsed.cabinClass : undefined,
      travelerIds: Array.isArray(parsed.travelerIds) ? parsed.travelerIds.filter((value): value is string => typeof value === 'string') : [],
    }
  } catch {
    return null
  }
}

function formatOrderLineItemTitle(
  orderLineItem: OrderLineItemResponse,
  snapshotDetails: FlightSnapshotSummary | null,
  currentLanguage: AppLanguage,
) {
  if (snapshotDetails) {
    if (snapshotDetails.airlineName || snapshotDetails.flightNumber) {
      return [snapshotDetails.airlineName, snapshotDetails.flightNumber].filter(Boolean).join(' ')
    }
    return localizeBookingKind(orderLineItem.orderItemKind, currentLanguage)
  }

  return orderLineItem.summaryLabel
}
