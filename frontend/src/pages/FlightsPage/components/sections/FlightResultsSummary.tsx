import type { FormEvent, ReactNode } from 'react'

import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'
import { useFlightResultsState } from '@/pages/FlightsPage/components/hooks/useFlightResultsState'
import type { DisplayFlight, FlightResultsRoute, FlightResultsSectionProps } from '../../objects'
import { formatFlightRouteCity, formatDateLabel, formatPrice, formatWeekday } from './FlightResultsUtils'
import { FlightResultsFilters } from './FlightResultsFilters'
import { FlightResultsCards, FlightTravelerSelectionPanel } from './FlightResultsCards'

export function FlightResultsSummary({
  route,
  resultsState,
  routeTabs,
  showRouteHeading,
  isBusy,
  travelers,
  selectedTravelerIds,
  onToggleTravelerSelection,
  translate,
  getLateBookingNotice,
  onDateSelect,
  onSubmit,
}: {
  route: FlightResultsRoute
  resultsState: ReturnType<typeof useFlightResultsState>
  routeTabs?: ReactNode
  showRouteHeading?: boolean
  isBusy: boolean
  travelers: FlightResultsSectionProps['travelers']
  selectedTravelerIds: string[]
  onToggleTravelerSelection: (travelerId: string) => void
  translate: (translationKey: string) => string
  getLateBookingNotice: (flightResponse: FlightPlannerResponse) => string
  onDateSelect: (date: string) => void
  onSubmit: (event: FormEvent<HTMLFormElement>, displayFlight: DisplayFlight, travelerIds: string[]) => Promise<void>
}) {
  return (
    <div className="grid gap-6 bg-slate-100">
      <FlightTravelerSelectionPanel travelers={travelers} selectedTravelerIds={selectedTravelerIds} onToggleTravelerSelection={onToggleTravelerSelection} />
      <DatePriceStrip
        prices={resultsState.dailyLowestPrices}
        selectedDate={route.departureDate}
        onPrevious={resultsState.showPreviousDateWindow}
        onNext={resultsState.showNextDateWindow}
        onDateSelect={onDateSelect}
      />

      {routeTabs ?? (showRouteHeading ? (
        <div className="flex flex-wrap items-end justify-between gap-4">
          <h3 className="m-0 text-3xl font-bold text-slate-950">
            <span className="mr-3 text-xl font-medium">航段：</span>
            {formatFlightRouteCity(route.departureAirport)}
            <span className="mx-3 text-slate-300">→</span>
            {formatFlightRouteCity(route.arrivalAirport)}
            <span className="ml-4 text-xl font-medium text-slate-700">{resultsState.formatRouteDate(route.departureDate)}</span>
          </h3>
        </div>
      ) : null)}

      <FlightResultsFilters
        airlineOptions={resultsState.airlineOptions}
        departureAirportOptions={resultsState.departureAirportOptions}
        arrivalAirportOptions={resultsState.arrivalAirportOptions}
        cabinOptions={resultsState.cabinOptions}
        selectedAirline={resultsState.selectedAirline}
        selectedTimeRange={resultsState.selectedTimeRange}
        selectedDepartureAirport={resultsState.selectedDepartureAirport}
        selectedArrivalAirport={resultsState.selectedArrivalAirport}
        selectedCabin={resultsState.selectedCabin}
        sortMode={resultsState.sortMode}
        onAirlineChange={resultsState.setSelectedAirline}
        onTimeRangeChange={resultsState.setSelectedTimeRange}
        onDepartureAirportChange={resultsState.setSelectedDepartureAirport}
        onArrivalAirportChange={resultsState.setSelectedArrivalAirport}
        onCabinChange={resultsState.setSelectedCabin}
        onSortModeChange={resultsState.setSortMode}
        formatAirportName={resultsState.formatAirportName}
        formatCabinLabel={resultsState.formatCabinLabel}
      />

      <FlightResultsCards
        displayFlights={resultsState.displayFlights}
        isBusy={isBusy}
        selectedTravelerIds={selectedTravelerIds}
        getLateBookingNotice={getLateBookingNotice}
        onSubmit={onSubmit}
        translate={translate}
      />
    </div>
  )
}

function DatePriceStrip({
  prices,
  selectedDate,
  onPrevious,
  onNext,
  onDateSelect,
}: {
  prices: Array<{ date: string; lowestPrice: string | null; currency: string | null }>
  selectedDate: string
  onPrevious: () => void
  onNext: () => void
  onDateSelect: (date: string) => void
}) {
  return (
    <div className="relative">
      <DateWindowButton direction="left" onClick={onPrevious} />
      <div className="grid grid-cols-2 bg-white sm:grid-cols-4 lg:grid-cols-7">
        {prices.map(item => {
          const isSelected = item.date === selectedDate
          return (
            <button
              type="button"
              key={item.date}
              className={`grid gap-1 border-r border-slate-200 px-4 py-4 text-center transition last:border-r-0 hover:bg-sky-50 ${
                isSelected ? 'bg-sky-500 text-white' : 'text-slate-700'
              }`}
              onClick={() => onDateSelect(item.date)}
            >
              <span className="text-base font-medium">{formatDateLabel(item.date)}</span>
              <span className="text-base">{formatWeekday(item.date)}</span>
              <strong className={`text-xl font-bold ${isSelected ? 'text-white' : 'text-orange-500'}`}>
                {item.lowestPrice ? formatPrice(item.lowestPrice) : '--'}
              </strong>
            </button>
          )
        })}
      </div>
      <DateWindowButton direction="right" onClick={onNext} />
    </div>
  )
}

function DateWindowButton({ direction, onClick }: { direction: 'left' | 'right'; onClick: () => void }) {
  return (
    <button
      type="button"
      className={`group absolute top-1/2 z-10 flex h-9 w-9 -translate-y-1/2 items-center justify-center rounded-full border border-slate-300 bg-white transition hover:bg-black ${
        direction === 'left' ? '-left-4' : '-right-4'
      }`}
      onClick={onClick}
      aria-label={direction === 'left' ? '查看前一天最低价' : '查看后一天最低价'}
    >
      <span
        className={`h-3 w-3 border-l-2 border-t-2 border-black transition group-hover:border-white ${
          direction === 'left' ? '-rotate-45' : 'rotate-[135deg]'
        }`}
      />
    </button>
  )
}
