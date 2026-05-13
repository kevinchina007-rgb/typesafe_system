import type { FormEvent } from 'react'

import type { AppLanguage, ResourceReviewSummaryResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'
import type { BookFlightRequest } from '@/microservices/flight/objects/BookFlightRequest'
import type { FlightResponse } from '@/lib/mvp-types/flights'
import { formatIsoDateTime } from '@/lib/presenters/view-models'
import { ResourceReviewSummaryLoader } from '@/pages/shared/content/ResourceReviewSummaryLoader'
import { formatFlightDuration, getFlightStatusLabel, getLocalizedCabinLabel, renderFlightTravelerOptionLabel } from '@/app/stores/models/flights/flightHelpers'

type FlightResultsSectionProps = {
  currentLanguage: AppLanguage
  flightResponses: FlightResponse[]
  hasSearchedFlights: boolean
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onBookFlight: (payload: BookFlightRequest) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
  onRequireLateBookingReview: (flightResponse: FlightResponse) => void
  getLateBookingNotice: (flightResponse: FlightResponse) => string
}

export function FlightResultsSection({
  currentLanguage,
  flightResponses,
  hasSearchedFlights,
  isBusy,
  isGuestMode,
  travelers,
  translate,
  onRequireLogin,
  onBookFlight,
  onLoadReviewSummary,
  onLoadReviews,
  onRequireLateBookingReview,
  getLateBookingNotice,
}: FlightResultsSectionProps) {
  async function handleFlightBooking(event: FormEvent<HTMLFormElement>, flightResponse: FlightResponse, cabinClass: string) {
    event.preventDefault()

    if (isGuestMode) {
      onRequireLogin()
      return
    }

    if (flightResponse.bookingWindowStatus === 'SurchargeRequired') {
      onRequireLateBookingReview(flightResponse)
      return
    }

    const formData = new FormData(event.currentTarget)
    const selectedTravelerIds = formData
      .getAll('travelerIds')
      .map(value => String(value))
      .filter(Boolean)

    await onBookFlight({
      flightId: flightResponse.flightId,
      travelerIds: selectedTravelerIds,
      cabinClass,
    })
  }

  if (!hasSearchedFlights) {
    return null
  }

  return (
    <section className="flight-results-section">
      <div className="flight-results-header">
        <div>
          <p className="eyebrow-label">{translate('flights.resultsEyebrow')}</p>
          <h3 className="section-title">{translate('flights.resultsTitle')}</h3>
        </div>
        <span className="tag-chip">
          {translate('flights.resultsCount').replace('{count}', String(flightResponses.length))}
        </span>
      </div>

      <div className="entity-list flights-list">
        {flightResponses.length > 0 ? (
          flightResponses.map(flightResponse => (
            <article key={flightResponse.flightId} className="panel-card flight-result-card">
              <div className="flight-result-top">
                <div className="flight-result-brand">
                  <strong>{`${flightResponse.airlineName} ${flightResponse.flightNumber}`}</strong>
                  <span>{`${flightResponse.departureAirport} -> ${flightResponse.arrivalAirport}`}</span>
                </div>
                <div className="flight-result-meta">
                  <span className="tag-chip">{getFlightStatusLabel(flightResponse.status, currentLanguage)}</span>
                  {flightResponse.bookingWindowStatus === 'SurchargeRequired' ? (
                    <span className="tag-chip tag-chip-warning">{translate('flights.surchargeRequired')}</span>
                  ) : null}
                  <ResourceReviewSummaryLoader
                    currentLanguage={currentLanguage}
                    isBusy={isBusy}
                    isEnabled={!isGuestMode}
                    resourceType="Flight"
                    resourceId={flightResponse.flightId}
                    title={`${flightResponse.airlineName} ${flightResponse.flightNumber}`}
                    translate={translate}
                    onLoadSummary={onLoadReviewSummary}
                    onLoadReviews={onLoadReviews}
                  />
                </div>
              </div>

              <div className="flight-result-timeline">
                <div className="flight-time-block">
                  <span className="detail-label">{translate('flights.departureTime')}</span>
                  <strong>{formatIsoDateTime(flightResponse.departureTime, '-')}</strong>
                  <small>{flightResponse.departureAirport}</small>
                </div>
                <div className="flight-duration-block">
                  <span>{formatFlightDuration(flightResponse.departureTime, flightResponse.arrivalTime)}</span>
                  <div className="flight-duration-line" />
                  <small>{translate('flights.directFlight')}</small>
                </div>
                <div className="flight-time-block">
                  <span className="detail-label">{translate('flights.arrivalTime')}</span>
                  <strong>{formatIsoDateTime(flightResponse.arrivalTime, '-')}</strong>
                  <small>{flightResponse.arrivalAirport}</small>
                </div>
              </div>

              <ul className="entity-list flight-cabin-list">
                {flightResponse.cabinInventories.map(cabinInventory => (
                  <li key={cabinInventory.inventoryId}>
                    <div className="flight-cabin-summary">
                      <strong>{getLocalizedCabinLabel(cabinInventory.cabinClass, currentLanguage)}</strong>
                      <p>{`${translate('flights.availableSeats')}: ${cabinInventory.availableSeats}`}</p>
                      <p className="flight-cabin-price">{`${cabinInventory.unitPrice} ${cabinInventory.currency}`}</p>
                    </div>
                    <form
                      className="compact-action-block flight-booking-form"
                      onSubmit={event => void handleFlightBooking(event, flightResponse, cabinInventory.cabinClass)}
                    >
                      <div className="checkbox-list">
                        <p className="detail-label">{translate('flights.selectTravelers')}</p>
                        {travelers.map(traveler => (
                          <label key={traveler.travelerId} className="checkbox-row">
                            <input
                              type="checkbox"
                              name="travelerIds"
                              value={traveler.travelerId}
                              disabled={isBusy || !cabinInventory.isBookable || flightResponse.bookingWindowStatus === 'Expired'}
                            />
                            {renderFlightTravelerOptionLabel(traveler)}
                          </label>
                        ))}
                      </div>
                      {flightResponse.bookingWindowStatus === 'SurchargeRequired' ? (
                        <p className="flight-booking-notice">{getLateBookingNotice(flightResponse)}</p>
                      ) : null}
                      <button type="submit" disabled={isBusy || !cabinInventory.isBookable}>
                        {translate('flights.bookNow')}
                      </button>
                    </form>
                  </li>
                ))}
              </ul>
            </article>
          ))
        ) : (
          <p className="empty-state">{translate('flights.empty')}</p>
        )}
      </div>
    </section>
  )
}





