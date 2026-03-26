import { useState } from 'react'

import type { AppLanguage, FlightResponse, OrderResponse, TravelerResponse } from '../lib/mvp-types'
import { formatIsoDateTime, localizeCabinClass, mapBackendStatusToProductLabel } from '../lib/view-models'

type FlightsPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  currentBooking: OrderResponse | null
  translate: (translationKey: string) => string
  onSearchFlights: (payload: {
    departureAirport?: string
    arrivalAirport?: string
    date?: string
  }) => Promise<FlightResponse[]>
  onCreateBookingShell: (payload: { orderCurrency: string }) => Promise<void>
  onAddFlightToBooking: (payload: {
    flightId: string
    travelerIds: string[]
    cabinClass: string
  }) => Promise<void>
}

function renderTravelerOptionLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

export function FlightsPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  currentBooking,
  translate,
  onSearchFlights,
  onCreateBookingShell,
  onAddFlightToBooking,
}: FlightsPanelProps) {
  const [flightResponses, setFlightResponses] = useState<FlightResponse[]>([])
  const [hasSearchedFlights, setHasSearchedFlights] = useState(false)

  return (
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.flights')}</p>
          <h2>{translate('flights.title')}</h2>
        </div>
      </div>

      <p className="hero-copy">{translate('flights.description')}</p>
      <p className="empty-state">{translate('flights.searchHint')}</p>

      <form
        className="stack-form panel-card"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          const nextFlightResponses = await onSearchFlights({
            departureAirport: String(formData.get('departureAirport') ?? '').trim() || undefined,
            arrivalAirport: String(formData.get('arrivalAirport') ?? '').trim() || undefined,
            date: String(formData.get('date') ?? '').trim() || undefined,
          })
          setHasSearchedFlights(true)
          setFlightResponses(nextFlightResponses)
        }}
      >
        <div className="three-column-grid">
          <label>
            {translate('flights.departureAirport')}
            <input name="departureAirport" placeholder="PVG / Shanghai / 上海" />
          </label>
          <label>
            {translate('flights.arrivalAirport')}
            <input name="arrivalAirport" placeholder="NRT / Tokyo / 东京" />
          </label>
          <label>
            {translate('flights.date')}
            <input name="date" type="date" defaultValue="2026-04-05" />
          </label>
        </div>

        <button type="submit" disabled={isBusy}>
          {translate('flights.search')}
        </button>
      </form>

      {!isGuestMode ? (
        <form
          className="inline-form"
          onSubmit={async event => {
            event.preventDefault()
            const formData = new FormData(event.currentTarget)
            await onCreateBookingShell({
              orderCurrency: String(formData.get('orderCurrency') ?? 'CNY'),
            })
          }}
        >
          <select name="orderCurrency" defaultValue={currentBooking?.orderCurrency ?? 'CNY'} disabled={isBusy}>
            <option value="CNY">CNY</option>
            <option value="USD">USD</option>
            <option value="EUR">EUR</option>
          </select>
          <button type="submit" disabled={isBusy}>
            {translate('flights.createBooking')}
          </button>
        </form>
      ) : null}

      {isGuestMode ? <p className="empty-state">{translate('flights.guest')}</p> : null}
      {!currentBooking && !isGuestMode ? <p className="empty-state">{translate('flights.requireBooking')}</p> : null}

      {hasSearchedFlights ? (
        <div className="entity-list flights-list">
          {flightResponses.length > 0 ? (
            flightResponses.map(flightResponse => (
              <article key={flightResponse.flightId} className="panel-card flight-card">
                <div className="panel-heading">
                  <div>
                    <strong>{`${flightResponse.airlineName} ${flightResponse.flightNumber}`}</strong>
                    <p>{`${flightResponse.departureAirport} -> ${flightResponse.arrivalAirport}`}</p>
                  </div>
                  <span className="tag-chip">{mapBackendStatusToProductLabel(flightResponse.status, currentLanguage)}</span>
                </div>

                <div className="detail-grid">
                  <div>
                    <span className="detail-label">{translate('flights.departureTime')}</span>
                    <strong>{formatIsoDateTime(flightResponse.departureTime, '-')}</strong>
                  </div>
                  <div>
                    <span className="detail-label">{translate('flights.arrivalTime')}</span>
                    <strong>{formatIsoDateTime(flightResponse.arrivalTime, '-')}</strong>
                  </div>
                  <div>
                    <span className="detail-label">{translate('flights.status')}</span>
                    <strong>{mapBackendStatusToProductLabel(flightResponse.status, currentLanguage)}</strong>
                  </div>
                </div>

                <ul className="entity-list">
                  {flightResponse.cabinInventories.map(cabinInventoryResponse => (
                    <li key={cabinInventoryResponse.inventoryId}>
                      <div>
                        <strong>{localizeCabinClass(cabinInventoryResponse.cabinClass, currentLanguage)}</strong>
                        <p>
                          {`${translate('flights.availableSeats')}: ${cabinInventoryResponse.availableSeats} · ${cabinInventoryResponse.unitPrice} ${cabinInventoryResponse.currency}`}
                        </p>
                      </div>
                      <form
                        className="compact-action-block"
                        onSubmit={async event => {
                          event.preventDefault()
                          const formData = new FormData(event.currentTarget)
                          const selectedTravelerIds = formData
                            .getAll('travelerIds')
                            .map(value => String(value))
                            .filter(Boolean)
                          await onAddFlightToBooking({
                            flightId: flightResponse.flightId,
                            travelerIds: selectedTravelerIds,
                            cabinClass: cabinInventoryResponse.cabinClass,
                          })
                        }}
                      >
                        <select name="travelerIds" multiple disabled={!currentBooking || isBusy || !cabinInventoryResponse.isBookable}>
                          {travelers.map(traveler => (
                            <option key={traveler.travelerId} value={traveler.travelerId}>
                              {renderTravelerOptionLabel(traveler)}
                            </option>
                          ))}
                        </select>
                        <button type="submit" disabled={!currentBooking || isBusy || !cabinInventoryResponse.isBookable}>
                          {translate('flights.addToBooking')}
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
      ) : null}
    </section>
  )
}
