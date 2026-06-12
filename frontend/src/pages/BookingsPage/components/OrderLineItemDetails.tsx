// 订单行项目详情组件，只负责把单个订单项的补充信息渲染出来。
import { formatIsoDateTime, localizeBookingKind, localizeCabinClass, localizeReservationStatus, localizeSupplierReviewStatus } from '@/lib/presenters/view-models'
import { formatTravelerIdentity, formatOrderLineItemTitle, parseAttractionSnapshot, parseFlightSnapshot } from '@/pages/BookingsPage/functions'
import type { OrderLineItemDetailsProps } from '@/pages/BookingsPage/objects'

export function OrderLineItemDetails({
  currentLanguage,
  orderLineItem,
  existingReview,
  travelers,
  translate,
}: OrderLineItemDetailsProps) {
  const snapshotDetails = parseFlightSnapshot(orderLineItem.summaryLabel)
  const attractionSnapshot = parseAttractionSnapshot(orderLineItem.summaryLabel)
  const hasStructuredDetails = !!(orderLineItem.flightDetails || orderLineItem.hotelDetails || orderLineItem.trainDetails || orderLineItem.attractionDetails || attractionSnapshot)

  return (
    <div className="grid gap-2 text-base text-slate-700">
      <strong className="text-xl font-bold text-slate-950">{formatOrderLineItemTitle(orderLineItem, snapshotDetails, currentLanguage)}</strong>
      <p className="m-0">{`${localizeBookingKind(orderLineItem.orderItemKind, currentLanguage)} | ${localizeSupplierReviewStatus(orderLineItem.supplierReviewStatus, currentLanguage)}`}</p>

      {!hasStructuredDetails && snapshotDetails ? (
        <>
          {snapshotDetails.flightId ? <p className="m-0">{`${translate('booking.flight.id')}: ${snapshotDetails.flightId}`}</p> : null}
          {snapshotDetails.cabinClass ? <p className="m-0">{`${translate('booking.flight.cabin')}: ${localizeCabinClass(snapshotDetails.cabinClass, currentLanguage)}`}</p> : null}
          {snapshotDetails.travelerIds.length > 0 ? (
            <p className="m-0">
              {`${translate('booking.flight.travelers')}: ${snapshotDetails.travelerIds.map(travelerId => formatTravelerIdentity(travelers, travelerId)).join('、')}`}
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
              {orderLineItem.flightDetails.reservationExpiresAt ? ` | ${translate('booking.flight.reservationExpiresAt')}: ${formatIsoDateTime(orderLineItem.flightDetails.reservationExpiresAt, translate('booking.notYet'))}` : ''}
            </p>
          ) : null}
        </>
      ) : null}

      {orderLineItem.hotelDetails ? (
        <>
          <p className="m-0">
            {`${translate('booking.hotel.roomType')}: ${orderLineItem.hotelDetails.roomTypeName} | ${translate('booking.hotel.guests')}: ${orderLineItem.hotelDetails.guestTravelerIds.length} | ${translate('booking.hotel.roomCount')}: ${orderLineItem.hotelDetails.roomCount}`}
          </p>
          <p className="m-0">{`${translate('booking.hotel.stay')}: ${orderLineItem.hotelDetails.checkInDate} -> ${orderLineItem.hotelDetails.checkOutDate}`}</p>
          {orderLineItem.hotelDetails.reservationStatus ? (
            <p className="m-0">
              {`${translate('booking.hotel.reservation')}: ${localizeReservationStatus(orderLineItem.hotelDetails.reservationStatus, currentLanguage)}`}
              {orderLineItem.hotelDetails.reservationExpiresAt ? ` | ${translate('booking.hotel.reservationExpiresAt')}: ${formatIsoDateTime(orderLineItem.hotelDetails.reservationExpiresAt, translate('booking.notYet'))}` : ''}
            </p>
          ) : null}
          <p className="m-0">
            {`${translate('booking.hotel.unitPrice')}: ${orderLineItem.hotelDetails.unitPrice} ${orderLineItem.hotelDetails.currency} | ${translate('booking.hotel.totalPrice')}: ${orderLineItem.hotelDetails.totalPrice} ${orderLineItem.hotelDetails.currency}`}
          </p>
        </>
      ) : null}

      {orderLineItem.attractionDetails || attractionSnapshot ? (
        <>
          <p className="m-0">
            {`${translate('booking.attraction.ticketType')}: ${orderLineItem.attractionDetails?.ticketTypeName ?? attractionSnapshot?.ticketTypeName ?? ''} | ${translate('booking.attraction.travelers')}: ${(orderLineItem.attractionDetails?.travelerIds ?? attractionSnapshot?.travelerIds ?? []).length}`}
          </p>
          <p className="m-0">{`${translate('booking.attraction.useDate')}: ${orderLineItem.attractionDetails?.useDate ?? attractionSnapshot?.useDate ?? ''}`}</p>
          <p className="m-0">
            {`${translate('booking.attraction.unitPrice')}: ${orderLineItem.attractionDetails?.unitPrice ?? attractionSnapshot?.unitPrice ?? ''} ${orderLineItem.attractionDetails?.currency ?? attractionSnapshot?.currency ?? orderLineItem.bookedCurrency} | ${translate('booking.attraction.totalPrice')}: ${orderLineItem.attractionDetails?.totalPrice ?? attractionSnapshot?.totalPrice ?? orderLineItem.bookedAmount} ${orderLineItem.attractionDetails?.currency ?? attractionSnapshot?.currency ?? orderLineItem.bookedCurrency}`}
          </p>
          {((orderLineItem.attractionDetails?.eligibilityRuleSummaries ?? attractionSnapshot?.eligibilityRuleSummaries ?? []).length > 0) ? (
            <p className="m-0">{`${translate('booking.attraction.rules')}: ${(orderLineItem.attractionDetails?.eligibilityRuleSummaries ?? attractionSnapshot?.eligibilityRuleSummaries ?? []).join(' | ')}`}</p>
          ) : null}
        </>
      ) : null}

      {orderLineItem.supplierReviewDecision?.reason ? <p className="m-0">{orderLineItem.supplierReviewDecision.reason}</p> : null}
      {existingReview ? <p className="m-0">{`${translate('reviews.alreadyWritten')}: ${existingReview.title}`}</p> : null}
    </div>
  )
}
