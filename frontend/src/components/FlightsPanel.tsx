import { useMemo, useState } from 'react'

import type { AppLanguage, FlightResponse, ResourceReviewSummaryResponse, ReviewResponse, TravelerResponse } from '../lib/mvp-types'
import { formatIsoDateTime, localizeCabinClass, mapBackendStatusToProductLabel } from '../lib/view-models'
import { FlightSearchCard } from './flights/FlightSearchCard'
import type { HotRoute } from './flights/HotRoutes'
import type { TripType } from './flights/TripTypeSelector'
import { ResourceReviewSummaryLoader } from './ResourceReviewSummaryLoader'

type FlightsPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
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
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}

type FlightSearchState = {
  tripType: TripType
  departureAirport: string
  arrivalAirport: string
  departureDate: string
  returnDate: string
  adults: number
  childrenCount: number
  cabinPreference: string
}

const defaultSearchState: FlightSearchState = {
  tripType: 'oneWay',
  departureAirport: 'Shanghai',
  arrivalAirport: 'Tokyo',
  departureDate: '2026-04-20',
  returnDate: '2026-04-27',
  adults: 1,
  childrenCount: 0,
  cabinPreference: 'Economy',
}

const hotRoutes: HotRoute[] = [
  { id: 'pek-sha', departureLabel: '北京', arrivalLabel: '上海' },
  { id: 'sha-tyo', departureLabel: '上海', arrivalLabel: '东京' },
  { id: 'can-sin', departureLabel: '广州', arrivalLabel: '新加坡' },
  { id: 'sha-hkg', departureLabel: '上海', arrivalLabel: '香港' },
]

const recentSearches = ['上海 / PVG', '东京 / NRT', '北京 / PEK', '新加坡 / SIN']
const popularCities = ['Shanghai', 'Beijing', 'Tokyo', 'Hong Kong', 'Singapore', 'Guangzhou']

function renderTravelerOptionLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

function formatFlightDuration(departureTime: string, arrivalTime: string) {
  const durationMs = new Date(arrivalTime).getTime() - new Date(departureTime).getTime()
  const totalMinutes = Math.max(0, Math.round(durationMs / 60000))
  const hours = Math.floor(totalMinutes / 60)
  const minutes = totalMinutes % 60
  return `${hours}h ${minutes}m`
}

function getLowestPriceLabel(flights: FlightResponse[], translate: (translationKey: string) => string) {
  const candidatePrices = flights.flatMap(flight =>
    [Number(flight.basePrice), ...flight.cabinInventories.map(cabinInventory => Number(cabinInventory.unitPrice))].filter(
      value => Number.isFinite(value),
    ),
  )

  if (candidatePrices.length === 0) {
    return translate('flights.priceInsightFallback')
  }

  return translate('flights.priceInsight').replace('{price}', String(Math.min(...candidatePrices)))
}

function getSuggestedTravelWindowLabel(flights: FlightResponse[], translate: (translationKey: string) => string) {
  if (flights.length === 0) {
    return translate('flights.bestWindowFallback')
  }

  const earliestFlight = [...flights].sort(
    (left, right) => new Date(left.departureTime).getTime() - new Date(right.departureTime).getTime(),
  )[0]

  const hour = new Date(earliestFlight.departureTime).getHours()
  if (hour < 10) {
    return translate('flights.bestWindowMorning')
  }
  if (hour < 16) {
    return translate('flights.bestWindowMidday')
  }
  return translate('flights.bestWindowEvening')
}

export function FlightsPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  translate,
  onRequireLogin,
  onSearchFlights,
  onBookFlight,
  onLoadReviewSummary,
  onLoadReviews,
}: FlightsPanelProps) {
  const [flightResponses, setFlightResponses] = useState<FlightResponse[]>([])
  const [hasSearchedFlights, setHasSearchedFlights] = useState(false)
  const [searchState, setSearchState] = useState<FlightSearchState>(defaultSearchState)

  const priceInsight = useMemo(
    () => getLowestPriceLabel(flightResponses, translate),
    [flightResponses, translate],
  )
  const travelWindowLabel = useMemo(
    () => getSuggestedTravelWindowLabel(flightResponses, translate),
    [flightResponses, translate],
  )

  async function submitSearch() {
    const nextFlights = await onSearchFlights({
      departureAirport: searchState.departureAirport.trim() || undefined,
      arrivalAirport: searchState.arrivalAirport.trim() || undefined,
      date: searchState.departureDate || undefined,
    })
    setHasSearchedFlights(true)
    setFlightResponses(nextFlights)
  }

  function updateSearchState<K extends keyof FlightSearchState>(key: K, value: FlightSearchState[K]) {
    setSearchState(currentState => ({
      ...currentState,
      [key]: value,
    }))
  }

  function applyQuickDatePreset(preset: 'today' | 'tomorrow' | 'weekend' | 'nextWeek') {
    const baseDate = new Date()
    const nextDate = new Date(baseDate)

    if (preset === 'tomorrow') {
      nextDate.setDate(baseDate.getDate() + 1)
    } else if (preset === 'weekend') {
      const day = baseDate.getDay()
      const offset = day === 6 ? 0 : day === 0 ? 6 : 6 - day
      nextDate.setDate(baseDate.getDate() + offset)
    } else if (preset === 'nextWeek') {
      nextDate.setDate(baseDate.getDate() + 7)
    }

    const formatted = nextDate.toISOString().slice(0, 10)
    updateSearchState('departureDate', formatted)

    if (searchState.tripType === 'roundTrip') {
      const returnDate = new Date(nextDate)
      returnDate.setDate(nextDate.getDate() + 4)
      updateSearchState('returnDate', returnDate.toISOString().slice(0, 10))
    }
  }

  return (
    <section className="page-card flight-booking-page">
      <div className="flight-page-header">
        <div>
          <p className="eyebrow-label">{translate('nav.flights')}</p>
          <h2>{translate('flights.title')}</h2>
          <p className="hero-copy">{translate('flights.description')}</p>
        </div>
      </div>

      <FlightSearchCard
        tripType={searchState.tripType}
        departureAirport={searchState.departureAirport}
        arrivalAirport={searchState.arrivalAirport}
        departureDate={searchState.departureDate}
        returnDate={searchState.returnDate}
        adults={searchState.adults}
        childrenCount={searchState.childrenCount}
        cabinPreference={searchState.cabinPreference}
        hotRoutes={hotRoutes}
        recentSearches={recentSearches}
        popularCities={popularCities}
        priceInsight={priceInsight}
        recommendationLabel={travelWindowLabel}
        translate={translate}
        onTripTypeChange={value => updateSearchState('tripType', value)}
        onDepartureAirportChange={value => updateSearchState('departureAirport', value)}
        onArrivalAirportChange={value => updateSearchState('arrivalAirport', value)}
        onDepartureDateChange={value => updateSearchState('departureDate', value)}
        onReturnDateChange={value => updateSearchState('returnDate', value)}
        onAdultsChange={value => updateSearchState('adults', value)}
        onChildrenChange={value => updateSearchState('childrenCount', value)}
        onCabinPreferenceChange={value => updateSearchState('cabinPreference', value)}
        onSelectRoute={route => {
          updateSearchState('departureAirport', route.departureLabel)
          updateSearchState('arrivalAirport', route.arrivalLabel)
        }}
        onQuickDateSelect={applyQuickDatePreset}
        onSubmit={() => void submitSearch()}
      />

      <section className="flight-recommendation-strip panel-card">
        <div className="flight-recommendation-item">
          <span className="flight-recommendation-label">{translate('flights.priceTrend')}</span>
          <strong>{priceInsight}</strong>
        </div>
        <div className="flight-recommendation-item">
          <span className="flight-recommendation-label">{translate('flights.bestTime')}</span>
          <strong>{travelWindowLabel}</strong>
        </div>
        <div className="flight-recommendation-item">
          <span className="flight-recommendation-label">{translate('flights.searchAssist')}</span>
          <strong>{translate('flights.searchHint')}</strong>
        </div>
      </section>

      {isGuestMode ? <p className="empty-state">{translate('flights.guest')}</p> : null}

      {hasSearchedFlights ? (
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
                      <span>{`${flightResponse.departureAirport} → ${flightResponse.arrivalAirport}`}</span>
                    </div>
                    <div className="flight-result-meta">
                      <span className="tag-chip">{mapBackendStatusToProductLabel(flightResponse.status, currentLanguage)}</span>
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
                          <strong>{localizeCabinClass(cabinInventory.cabinClass, currentLanguage)}</strong>
                          <p>{`${translate('flights.availableSeats')}: ${cabinInventory.availableSeats}`}</p>
                          <p className="flight-cabin-price">{`${cabinInventory.unitPrice} ${cabinInventory.currency}`}</p>
                        </div>
                        <form
                          className="compact-action-block flight-booking-form"
                          onSubmit={async event => {
                            event.preventDefault()
                            if (isGuestMode) {
                              onRequireLogin()
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
                                  disabled={isBusy || !cabinInventory.isBookable}
                                />
                                {renderTravelerOptionLabel(traveler)}
                              </label>
                            ))}
                          </div>
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
      ) : null}
    </section>
  )
}
