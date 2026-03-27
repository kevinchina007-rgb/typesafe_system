import { useState } from 'react'

import type { AppLanguage, FlightResponse, TravelerResponse } from '../lib/mvp-types'
import { formatIsoDateTime, localizeCabinClass, mapBackendStatusToProductLabel } from '../lib/view-models'

type FlightsPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onSearchFlights: (payload: {
    departureAirport?: string
    arrivalAirport?: string
    date?: string
  }) => Promise<FlightResponse[]>
  onBookFlight: (payload: {
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
  translate,
  onSearchFlights,
  onBookFlight,
}: FlightsPanelProps) {
  const [flightResponses, setFlightResponses] = useState<FlightResponse[]>([])
  const [hasSearchedFlights, setHasSearchedFlights] = useState(false)
  const [searchDate, setSearchDate] = useState('2026-04-05')

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
          const nextDate = String(formData.get('date') ?? '').trim()
          setSearchDate(nextDate)
          const nextFlights = await onSearchFlights({
            departureAirport: String(formData.get('departureAirport') ?? '').trim() || undefined,
            arrivalAirport: String(formData.get('arrivalAirport') ?? '').trim() || undefined,
            date: nextDate || undefined,
          })
          setHasSearchedFlights(true)
          setFlightResponses(nextFlights)
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
            <input name="date" type="date" defaultValue={searchDate} />
          </label>
        </div>

        <button type="submit" disabled={isBusy}>
          {translate('flights.search')}
        </button>
      </form>

      {isGuestMode ? <p className="empty-state">{translate('flights.guest')}</p> : null}

      {hasSearchedFlights ? (
        <div className="entity-list flights-list">
          {flightResponses.length > 0 ? (
            flightResponses.map(flightResponse => (
              <article key={flightResponse.flightId} className="panel-card hotel-card">
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
                </div>

                <ul className="entity-list">
                  {flightResponse.cabinInventories.map(cabinInventory => (
                    <li key={cabinInventory.inventoryId}>
                      <div>
                        <strong>{localizeCabinClass(cabinInventory.cabinClass, currentLanguage)}</strong>
                        <p>{`${translate('flights.availableSeats')}: ${cabinInventory.availableSeats}`}</p>
                        <p>{`${cabinInventory.unitPrice} ${cabinInventory.currency}`}</p>
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
                          await onBookFlight({
                            flightId: flightResponse.flightId,
                            travelerIds: selectedTravelerIds,
                            cabinClass: cabinInventory.cabinClass,
                          })
                        }}
                      >
                        <div className="checkbox-list">
                          <p className="detail-label">{translate('flights.selectTravelers')}</p>
                          {travelers.map(traveler => (
                            <label key={traveler.travelerId} className="checkbox-row">
                              <input
                                type="checkbox"
                                name="travelerIds"
                                value={traveler.travelerId}
                                disabled={isGuestMode || isBusy || !cabinInventory.isBookable}
                              />
                              {renderTravelerOptionLabel(traveler)}
                            </label>
                          ))}
                        </div>
                        <button type="submit" disabled={isGuestMode || isBusy || !cabinInventory.isBookable}>
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
      ) : null}
    </section>
  )
}
