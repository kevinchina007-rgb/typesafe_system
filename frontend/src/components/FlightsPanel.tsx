import { useState } from 'react'

import type { FlightResponse } from '../lib/mvp-types/flights'
import { FlightBookingWindowDialog } from './flights/dialogs/FlightBookingWindowDialog'
import { useFlightSearchState } from './flights/hooks/useFlightSearchState'
import {
  flightHotRoutes,
  getLowestPriceLabel,
  getSuggestedTravelWindowLabel,
  popularFlightCities,
  recentFlightSearches,
} from './flights/flightSearchModel'
import { buildFlightSearchRequest, buildLateBookingNotice } from './flights/models/flightPanelHelpers'
import type { FlightsPanelProps } from './flights/models/flightTypes'
import { FlightPageHero } from './flights/sections/FlightPageHero'
import { FlightResultsSection } from './flights/sections/FlightResultsSection'
import { FlightSearchCard } from './flights/sections/FlightSearchCard'

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
  const [lateBookingFlight, setLateBookingFlight] = useState<FlightResponse | null>(null)
  const {
    searchState,
    flightResponses,
    hasSearchedFlights,
    setFlightResponses,
    setHasSearchedFlights,
    updateSearchState,
    applyQuickDatePreset,
    updateTripType,
    updateMultiCitySegment,
    addMultiCitySegment,
    removeMultiCitySegment,
    applyRouteSelection,
  } = useFlightSearchState()

  async function submitSearch() {
    const nextFlights = await onSearchFlights(buildFlightSearchRequest(searchState))
    setHasSearchedFlights(true)
    setFlightResponses(nextFlights)
  }

  return (
    <>
      <section className="page-card flight-booking-page">
        <FlightPageHero translate={translate} />

        <FlightSearchCard
          tripType={searchState.tripType}
          departureAirport={searchState.departureAirport}
          arrivalAirport={searchState.arrivalAirport}
          departureDate={searchState.departureDate}
          returnDate={searchState.returnDate}
          selectedQuickDatePreset={searchState.selectedQuickDatePreset}
          multiCitySegments={searchState.multiCitySegments}
          adults={searchState.adults}
          childrenCount={searchState.childrenCount}
          cabinPreference={searchState.cabinPreference}
          hotRoutes={flightHotRoutes}
          recentSearches={recentFlightSearches}
          popularCities={popularFlightCities}
          priceInsight={getLowestPriceLabel(flightResponses, translate)}
          recommendationLabel={getSuggestedTravelWindowLabel(flightResponses, translate)}
          translate={translate}
          onTripTypeChange={updateTripType}
          onDepartureAirportChange={value => updateSearchState('departureAirport', value)}
          onArrivalAirportChange={value => updateSearchState('arrivalAirport', value)}
          onDepartureDateChange={value => updateSearchState('departureDate', value)}
          onReturnDateChange={value => updateSearchState('returnDate', value)}
          onMultiCitySegmentChange={updateMultiCitySegment}
          onAddMultiCitySegment={addMultiCitySegment}
          onRemoveMultiCitySegment={removeMultiCitySegment}
          onAdultsChange={value => updateSearchState('adults', value)}
          onChildrenChange={value => updateSearchState('childrenCount', value)}
          onCabinPreferenceChange={value => updateSearchState('cabinPreference', value)}
          onSelectRoute={route => applyRouteSelection(route.departureLabel, route.arrivalLabel)}
          onQuickDateSelect={applyQuickDatePreset}
          onSubmit={() => void submitSearch()}
        />

        {isGuestMode ? <p className="empty-state">{translate('flights.guest')}</p> : null}
        <FlightResultsSection
          currentLanguage={currentLanguage}
          flightResponses={flightResponses}
          hasSearchedFlights={hasSearchedFlights}
          isBusy={isBusy}
          isGuestMode={isGuestMode}
          travelers={travelers}
          translate={translate}
          onRequireLogin={onRequireLogin}
          onBookFlight={onBookFlight}
          onLoadReviewSummary={onLoadReviewSummary}
          onLoadReviews={onLoadReviews}
          onRequireLateBookingReview={setLateBookingFlight}
          getLateBookingNotice={flightResponse => buildLateBookingNotice(flightResponse, translate)}
        />
      </section>

      <FlightBookingWindowDialog
        flight={lateBookingFlight}
        translate={translate}
        onClose={() => setLateBookingFlight(null)}
      />
    </>
  )
}
