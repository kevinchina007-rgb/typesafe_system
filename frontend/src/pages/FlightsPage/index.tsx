import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
import { AdvertisementCardRail } from '@/pages/shared/advertising/sections/AdvertisementCardRail'
import { FlightBookingWindowDialog, FlightPageHero, FlightResultsSection, FlightSearchCard } from './components'
import { buildLateBookingNotice } from './functions'
import { useFlightsPageController } from './hooks'
import type { FlightsPageProps } from './objects'

export function FlightsPage(props: FlightsPageProps) {
  const {
    searchState,
    flightResponses,
    flightResultGroups,
    targetFlightResponses,
    targetFlightResultGroups,
    hasSearchedFlights,
    travelers,
    selectedTravelerIds,
    toggleTravelerSelection,
    isBusy,
    isAuthDialogOpen,
    lateBookingFlight,
    signedInUserId,
    isGuestMode,
    isTourGroupTargetMode,
    deliveryAdvertisements,
    handleOpenAdvertisement,
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
    initialSelectedCabin,
  } = useFlightsPageController(props)
  const { translate, onNavigate } = props
  const displayFlightResponses = isTourGroupTargetMode && targetFlightResponses.length > 0 ? targetFlightResponses : flightResponses
  const displayFlightResultGroups = isTourGroupTargetMode && targetFlightResultGroups.length > 0 ? targetFlightResultGroups : flightResultGroups
  const displayHasSearchedFlights = isTourGroupTargetMode ? displayFlightResponses.length > 0 : hasSearchedFlights

  return (
    <>
      <section className="mx-auto flex min-h-[calc(100vh-11rem)] w-full max-w-7xl flex-col bg-white text-slate-950">
        <FlightPageHero title={translate('flights.title')} description={translate('flights.description')} />

        {!isTourGroupTargetMode ? (
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
        ) : (
          <section className="grid gap-2 border border-slate-200 bg-sky-50 px-6 py-4 text-slate-950">
            <p className="text-sm font-bold text-sky-700">团内定向预订</p>
            <p className="text-base text-slate-700">已定位到对应航班，直接在下方完成预订。</p>
          </section>
        )}

          <FlightResultsSection
            searchState={searchState}
            flightResponses={displayFlightResponses}
            flightResultGroups={displayFlightResultGroups}
            hasSearchedFlights={displayHasSearchedFlights}
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
          initialSelectedCabin={initialSelectedCabin}
        />

        {!isTourGroupTargetMode && deliveryAdvertisements.length > 0 ? (
          <section className="mt-auto grid gap-4 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
            <p className="text-sm font-bold text-slate-500">{translate('advertising.deliveryEyebrow')}</p>
            <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('advertising.deliveryTitle')}</h3>
            <AdvertisementCardRail
              advertisements={deliveryAdvertisements}
              translate={translate}
              onOpenAdvertisement={handleOpenAdvertisement}
            />
          </section>
        ) : null}
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
