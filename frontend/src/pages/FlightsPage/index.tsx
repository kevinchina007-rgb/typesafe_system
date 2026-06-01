import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
import { FlightBookingWindowDialog, FlightResultsSection, FlightSearchCard } from './components'
import { buildLateBookingNotice } from './functions'
import { useFlightsPageController } from './hooks'
import type { FlightsPageProps } from './objects'

export function FlightsPage(props: FlightsPageProps) {
  const {
    searchState,
    flightResponses,
    flightResultGroups,
    hasSearchedFlights,
    travelers,
    selectedTravelerIds,
    toggleTravelerSelection,
    isBusy,
    isAuthDialogOpen,
    lateBookingFlight,
    signedInUserId,
    isGuestMode,
    openAuthDialog,
    closeAuthDialog,
    openLateBookingReview,
    closeLateBookingReview,
    updateSearchState,
    updateTripType,
    updateMultiCitySegment,
    addMultiCitySegment,
    removeMultiCitySegment,
    submitSearch,
    searchFlights,
    loadDailyLowestPrices,
    bookFlight,
  } = useFlightsPageController(props)
  const { translate, onNavigate } = props

  return (
    <>
      <section className="mx-auto flex min-h-[calc(100vh-11rem)] w-full max-w-7xl flex-col bg-white text-slate-950">
        <FlightSearchCard
          tripType={searchState.tripType}
          departureAirport={searchState.departureAirport}
          arrivalAirport={searchState.arrivalAirport}
          departureDate={searchState.departureDate}
          returnDate={searchState.returnDate}
          multiCitySegments={searchState.multiCitySegments}
          translate={translate}
          onTripTypeChange={updateTripType}
          onDepartureAirportChange={value => updateSearchState('departureAirport', value)}
          onArrivalAirportChange={value => updateSearchState('arrivalAirport', value)}
          onDepartureDateChange={value => updateSearchState('departureDate', value)}
          onReturnDateChange={value => updateSearchState('returnDate', value)}
          onMultiCitySegmentChange={updateMultiCitySegment}
          onAddMultiCitySegment={addMultiCitySegment}
          onRemoveMultiCitySegment={removeMultiCitySegment}
          onSwapRoute={() => {
            updateSearchState('departureAirport', searchState.arrivalAirport)
            updateSearchState('arrivalAirport', searchState.departureAirport)
          }}
          showSubmitButton={!hasSearchedFlights}
          onSubmit={() => void submitSearch()}
        />

        <FlightResultsSection
          searchState={searchState}
          flightResponses={flightResponses}
          flightResultGroups={flightResultGroups}
          hasSearchedFlights={hasSearchedFlights}
          isBusy={isBusy}
          isGuestMode={isGuestMode}
          signedInUserId={signedInUserId}
          travelers={travelers}
          selectedTravelerIds={selectedTravelerIds}
          onToggleTravelerSelection={toggleTravelerSelection}
          translate={translate}
          onRequireLogin={openAuthDialog}
          onBookFlight={bookFlight}
          onSearchFlights={searchFlights}
          onLoadDailyLowestPrices={loadDailyLowestPrices}
          onDepartureDateChange={value => updateSearchState('departureDate', value)}
          onReturnDateChange={value => updateSearchState('returnDate', value)}
          onMultiCitySegmentChange={updateMultiCitySegment}
          onRequireLateBookingReview={openLateBookingReview}
          getLateBookingNotice={flightResponse => buildLateBookingNotice(flightResponse, translate)}
        />

        <div className="mt-auto h-36 border-2 border-dashed border-slate-200 bg-white" aria-label="horizontal-ad-slot" />
      </section>

      <FlightBookingWindowDialog
        flight={lateBookingFlight}
        translate={translate}
        onClose={closeLateBookingReview}
      />

      <AuthRequiredDialog
        isOpen={isAuthDialogOpen}
        title={translate('authRequired.bookingTitle')}
        description={translate('authRequired.bookingDescription')}
        translate={translate}
        onClose={closeAuthDialog}
        onConfirm={() => {
          closeAuthDialog()
          onNavigate('account')
        }}
      />
    </>
  )
}
