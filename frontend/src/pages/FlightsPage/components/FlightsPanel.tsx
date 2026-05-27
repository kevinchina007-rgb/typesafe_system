import { useCallback, useEffect, useMemo, useRef, useState } from 'react'

import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'
import { buildLateBookingNotice, loadFlightResultGroups, validateFlightSearchState } from '@/app/stores/models/flights/flightPanelHelpers'
import type { FlightsPanelProps } from '@/app/stores/models/flights/flightTypes'
import { FlightBookingWindowDialog } from '@/pages/FlightsPage/components/dialogs/FlightBookingWindowDialog'
import { useFlightSearchState } from '@/pages/FlightsPage/components/hooks/useFlightSearchState'
import { FlightResultsSection } from '@/pages/FlightsPage/components/sections/FlightResultsSection'
import { FlightSearchCard } from '@/pages/FlightsPage/components/sections/FlightSearchCard'

export function FlightsPanel({
  isBusy,
  isGuestMode,
  signedInUserId,
  travelers,
  translate,
  onRequireLogin,
  onSearchFlights,
  onLoadDailyLowestPrices,
  onBookFlight,
  onValidationError,
}: FlightsPanelProps) {
  const [lateBookingFlight, setLateBookingFlight] = useState<FlightPlannerResponse | null>(null)
  const {
    searchState,
    flightResponses,
    flightResultGroups,
    hasSearchedFlights,
    setFlightPlannerResponses,
    setFlightResultGroups,
    setHasSearchedFlights,
    updateSearchState,
    updateTripType,
    updateMultiCitySegment,
    addMultiCitySegment,
    removeMultiCitySegment,
  } = useFlightSearchState()

  const searchKey = useMemo(() => JSON.stringify(searchState), [searchState])
  const lastSubmittedSearchKey = useRef<string | null>(null)
  const lastReportedErrorKey = useRef<string | null>(null)

  const reportErrorOnce = useCallback(
    (message: string) => {
      const nextErrorKey = `${searchKey}:${message}`
      if (lastReportedErrorKey.current === nextErrorKey) {
        return
      }

      lastReportedErrorKey.current = nextErrorKey
      onValidationError(message)
    },
    [onValidationError, searchKey],
  )

  const submitSearch = useCallback(async () => {
    const validationMessage = validateFlightSearchState(searchState)
    if (validationMessage) {
      reportErrorOnce(validationMessage)
      return
    }

    try {
      const nextResultGroups = await loadFlightResultGroups(searchState, onSearchFlights)
      const nextFlights = nextResultGroups.flatMap(resultGroup => resultGroup.flightResponses)
      lastReportedErrorKey.current = null
      lastSubmittedSearchKey.current = searchKey
      setFlightResultGroups(nextResultGroups)
      setFlightPlannerResponses(nextFlights)
      setHasSearchedFlights(nextResultGroups.length > 0)
    } catch {
      setFlightResultGroups([])
      setFlightPlannerResponses([])
      setHasSearchedFlights(false)
      reportErrorOnce('航班接口摔了一跤：请先确认后端已重启，并且新的航班演示数据迁移已经跑完。')
    }
  }, [onSearchFlights, reportErrorOnce, searchKey, searchState, setFlightPlannerResponses, setFlightResultGroups, setHasSearchedFlights])

  useEffect(() => {
    if (!hasSearchedFlights || lastSubmittedSearchKey.current === searchKey) {
      return
    }

    const timer = window.setTimeout(() => {
      void submitSearch()
    }, 250)

    return () => window.clearTimeout(timer)
  }, [hasSearchedFlights, searchKey, submitSearch])

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
          translate={translate}
          onRequireLogin={onRequireLogin}
          onBookFlight={onBookFlight}
          onSearchFlights={onSearchFlights}
          onLoadDailyLowestPrices={onLoadDailyLowestPrices}
          onDepartureDateChange={value => updateSearchState('departureDate', value)}
          onReturnDateChange={value => updateSearchState('returnDate', value)}
          onMultiCitySegmentChange={updateMultiCitySegment}
          onRequireLateBookingReview={setLateBookingFlight}
          getLateBookingNotice={flightResponse => buildLateBookingNotice(flightResponse, translate)}
        />

        <div className="mt-auto h-36 border-2 border-dashed border-slate-200 bg-white" aria-label="横版广告占位" />
      </section>

      <FlightBookingWindowDialog
        flight={lateBookingFlight}
        translate={translate}
        onClose={() => setLateBookingFlight(null)}
      />
    </>
  )
}
