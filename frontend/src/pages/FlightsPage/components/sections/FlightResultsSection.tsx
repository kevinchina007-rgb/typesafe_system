import { useEffect, useMemo, useState, type FocusEvent, type FormEvent, type ReactNode } from 'react'

import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'
import type { FlightSearchState } from '@/app/stores/models/flights'
import { formatFlightRouteCity } from '@/app/stores/models/flights/flightConstants'
import { departureTimeWindows } from '@/pages/FlightsPage/functions'
import { useFlightResultsState } from '@/pages/FlightsPage/components/hooks/useFlightResultsState'
import type { DisplayFlight, FlightResultsRoute, FlightResultsSectionProps, FlightSortMode } from '../../objects'

type RoundTripLeg = 'outbound' | 'return'

const legTheme = {
  outbound: {
    shell: 'bg-[#fff4cf]',
    activeTab: 'bg-[#f2c94c] border-[#c99a15] text-slate-950',
    inactiveTab: 'bg-[#fff9df] border-[#e7d99a] text-slate-700',
  },
  return: {
    shell: 'bg-[#eef7dc]',
    activeTab: 'bg-[#b8d96f] border-[#7fa83a] text-slate-950',
    inactiveTab: 'bg-[#f6fbec] border-[#d7e8b2] text-slate-700',
  },
  single: {
    shell: 'bg-slate-100',
    activeTab: 'bg-sky-500 border-sky-600 text-white',
    inactiveTab: 'bg-white border-slate-300 text-slate-700',
  },
}

export function FlightResultsSection({
  searchState,
  flightResponses,
  flightResultGroups,
  hasSearchedFlights,
  isBusy,
  isGuestMode,
  signedInUserId,
  travelers,
  selectedTravelerIds,
  onToggleTravelerSelection,
  translate,
  onRequireLogin,
  onBookFlight,
  onSearchFlights,
  onLoadDailyLowestPrices,
  onDepartureDateChange,
  onReturnDateChange,
  onMultiCitySegmentChange,
  onRequireLateBookingReview,
  getLateBookingNotice,
  initialSelectedCabin = 'all',
}: FlightResultsSectionProps) {
  const [activeLeg, setActiveLeg] = useState<RoundTripLeg>('outbound')
  const [activeMultiCitySegmentId, setActiveMultiCitySegmentId] = useState<string | null>(null)
  const isRoundTrip = searchState.tripType === 'roundTrip'
  const isMultiCity = searchState.tripType === 'multiCity'
  const multiCitySegments = searchState.multiCitySegments

  useEffect(() => {
    if (!isMultiCity) {
      setActiveMultiCitySegmentId(null)
      return
    }

    const firstSegment = multiCitySegments[0]
    if (!firstSegment) {
      setActiveMultiCitySegmentId(null)
      return
    }

    if (!activeMultiCitySegmentId || !multiCitySegments.some(segment => segment.id === activeMultiCitySegmentId)) {
      setActiveMultiCitySegmentId(firstSegment.id)
    }
  }, [activeMultiCitySegmentId, isMultiCity, multiCitySegments])

  const activeRoute = useMemo<FlightResultsRoute>(() => {
    if (isMultiCity) {
      const segment = multiCitySegments.find(nextSegment => nextSegment.id === activeMultiCitySegmentId) ?? multiCitySegments[0]
      return {
        departureAirport: segment?.departureAirport ?? '',
        arrivalAirport: segment?.arrivalAirport ?? '',
        departureDate: segment?.departureDate ?? '',
      }
    }

    if (isRoundTrip && activeLeg === 'return') {
      return {
        departureAirport: searchState.arrivalAirport,
        arrivalAirport: searchState.departureAirport,
        departureDate: searchState.returnDate,
      }
    }

    return {
      departureAirport: searchState.departureAirport,
      arrivalAirport: searchState.arrivalAirport,
      departureDate: searchState.departureDate,
    }
  }, [activeLeg, activeMultiCitySegmentId, isMultiCity, isRoundTrip, multiCitySegments, searchState.arrivalAirport, searchState.departureAirport, searchState.departureDate, searchState.returnDate])

  const activeFlights = useMemo(() => {
    if (isMultiCity) {
      const activeId = activeMultiCitySegmentId ?? multiCitySegments[0]?.id
      const group = flightResultGroups.find(resultGroup => resultGroup.id === activeId)
      return group?.flightResponses ?? []
    }

    if (!isRoundTrip) {
      return flightResultGroups.length > 0 ? flightResultGroups.flatMap(group => group.flightResponses) : flightResponses
    }

    const group = flightResultGroups.find(resultGroup => resultGroup.id === activeLeg)
    return group?.flightResponses ?? []
  }, [activeLeg, activeMultiCitySegmentId, flightResponses, flightResultGroups, isMultiCity, isRoundTrip, multiCitySegments])

  const resultsState = useFlightResultsState({
    route: activeRoute,
    searchedFlights: activeFlights,
    hasSearchedFlights,
    theme: isRoundTrip ? activeLeg : isMultiCity ? 'outbound' : 'single',
    onSearchFlights,
    onLoadDailyLowestPrices,
    initialSelectedCabin,
  })

  function handleDateSelect(date: string) {
    if (isMultiCity) {
      const segmentId = activeMultiCitySegmentId ?? multiCitySegments[0]?.id
      if (segmentId) {
        onMultiCitySegmentChange(segmentId, 'departureDate', date)
      }
      return
    }

    if (isRoundTrip && activeLeg === 'return') {
      onReturnDateChange(date)
      return
    }

    onDepartureDateChange(date)
  }

  async function handleFlightBooking(event: FormEvent<HTMLFormElement>, displayFlight: DisplayFlight, travelerIds: string[]) {
    event.preventDefault()

    if (isGuestMode) {
      onRequireLogin()
      return
    }

    if (!signedInUserId) {
      onRequireLogin()
      return
    }

    if (displayFlight.flight.bookingWindowStatus === 'SurchargeRequired') {
      onRequireLateBookingReview(displayFlight.flight)
      return
    }

    await onBookFlight({
      userId: signedInUserId,
      flightId: displayFlight.flight.flightId,
      travelerIds,
      cabinClass: displayFlight.displayCabinClass,
    })
  }

  if (!hasSearchedFlights) {
    return null
  }

  return (
    <section className="relative z-0 grid gap-6 bg-slate-100 px-6 pb-8 pt-10">
      {isRoundTrip ? (
      <ResultsBody
          route={activeRoute}
          resultsState={resultsState}
          routeTabs={
            <RoundTripTabs
              activeLeg={activeLeg}
              outboundRoute={{
                departureAirport: searchState.departureAirport,
                arrivalAirport: searchState.arrivalAirport,
                departureDate: searchState.departureDate,
              }}
              returnRoute={{
                departureAirport: searchState.arrivalAirport,
                arrivalAirport: searchState.departureAirport,
                departureDate: searchState.returnDate,
              }}
              onChange={setActiveLeg}
            />
          }
          isBusy={isBusy}
          travelers={travelers}
          selectedTravelerIds={selectedTravelerIds}
          onToggleTravelerSelection={onToggleTravelerSelection}
          translate={translate}
          getLateBookingNotice={getLateBookingNotice}
          onDateSelect={handleDateSelect}
          onSubmit={handleFlightBooking}
        />
      ) : isMultiCity ? (
        <ResultsBody
          route={activeRoute}
          resultsState={resultsState}
          routeTabs={
            <MultiCityTabs
              activeSegmentId={activeMultiCitySegmentId ?? multiCitySegments[0]?.id ?? ''}
              segments={multiCitySegments}
              onChange={setActiveMultiCitySegmentId}
            />
          }
          isBusy={isBusy}
          travelers={travelers}
          selectedTravelerIds={selectedTravelerIds}
          onToggleTravelerSelection={onToggleTravelerSelection}
          translate={translate}
          getLateBookingNotice={getLateBookingNotice}
          onDateSelect={handleDateSelect}
          onSubmit={handleFlightBooking}
        />
      ) : (
        <ResultsBody
          route={activeRoute}
          resultsState={resultsState}
          showRouteHeading
          isBusy={isBusy}
          travelers={travelers}
          selectedTravelerIds={selectedTravelerIds}
          onToggleTravelerSelection={onToggleTravelerSelection}
          translate={translate}
          getLateBookingNotice={getLateBookingNotice}
          onDateSelect={handleDateSelect}
          onSubmit={handleFlightBooking}
        />
      )}
    </section>
  )
}

function RoundTripTabs({
  activeLeg,
  outboundRoute,
  returnRoute,
  onChange,
}: {
  activeLeg: RoundTripLeg
  outboundRoute: FlightResultsRoute
  returnRoute: FlightResultsRoute
  onChange: (leg: RoundTripLeg) => void
}) {
  return (
    <div className="flex flex-wrap items-end gap-3">
      <LegTab
        isActive={activeLeg === 'outbound'}
        theme={legTheme.outbound}
        title="去程："
        route={outboundRoute}
        onClick={() => onChange('outbound')}
      />
      <LegTab
        isActive={activeLeg === 'return'}
        theme={legTheme.return}
        title="返程："
        route={returnRoute}
        onClick={() => onChange('return')}
      />
    </div>
  )
}

function MultiCityTabs({
  activeSegmentId,
  segments,
  onChange,
}: {
  activeSegmentId: string
  segments: FlightSearchState['multiCitySegments']
  onChange: (segmentId: string) => void
}) {
  const [firstVisibleIndex, setFirstVisibleIndex] = useState(0)
  const visibleCount = 2
  const maxFirstIndex = Math.max(segments.length - visibleCount, 0)
  const visibleSegments = segments.slice(firstVisibleIndex, firstVisibleIndex + visibleCount)

  useEffect(() => {
    const activeIndex = segments.findIndex(segment => segment.id === activeSegmentId)
    if (activeIndex < 0) {
      return
    }

    if (activeIndex < firstVisibleIndex) {
      setFirstVisibleIndex(activeIndex)
    } else if (activeIndex >= firstVisibleIndex + visibleCount) {
      setFirstVisibleIndex(Math.min(activeIndex - visibleCount + 1, maxFirstIndex))
    }
  }, [activeSegmentId, firstVisibleIndex, maxFirstIndex, segments])

  return (
    <div className="relative flex items-center gap-3">
      {segments.length > visibleCount ? (
        <TabWindowButton
          direction="left"
          disabled={firstVisibleIndex <= 0}
          onClick={() => setFirstVisibleIndex(index => Math.max(index - 1, 0))}
        />
      ) : null}
      <div className="flex min-w-0 flex-1 flex-wrap items-end gap-3 overflow-hidden">
        {visibleSegments.map((segment, index) => (
          <LegTab
            key={segment.id}
            isActive={activeSegmentId === segment.id}
            theme={legTheme.outbound}
            title={`第${firstVisibleIndex + index + 1}程：`}
            route={{
              departureAirport: segment.departureAirport,
              arrivalAirport: segment.arrivalAirport,
              departureDate: segment.departureDate,
            }}
            onClick={() => onChange(segment.id)}
          />
        ))}
      </div>
      {segments.length > visibleCount ? (
        <TabWindowButton
          direction="right"
          disabled={firstVisibleIndex >= maxFirstIndex}
          onClick={() => setFirstVisibleIndex(index => Math.min(index + 1, maxFirstIndex))}
        />
      ) : null}
    </div>
  )
}

function TabWindowButton({
  direction,
  disabled,
  onClick,
}: {
  direction: 'left' | 'right'
  disabled: boolean
  onClick: () => void
}) {
  return (
    <button
      type="button"
      className="group flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-slate-300 bg-white transition hover:bg-black disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-white"
      disabled={disabled}
      onClick={onClick}
    >
      <span
        className={`block h-3 w-3 rotate-45 border-b-2 border-l-2 border-black transition group-hover:border-white ${
          direction === 'right' ? 'rotate-[225deg]' : ''
        }`}
      />
    </button>
  )
}

function LegTab({
  isActive,
  theme,
  title,
  route,
  onClick,
}: {
  isActive: boolean
  theme: typeof legTheme.outbound
  title: string
  route: FlightResultsRoute
  onClick: () => void
}) {
  return (
    <button
      type="button"
      className={`px-6 py-4 text-left transition ${isActive ? theme.activeTab : theme.inactiveTab}`}
      onClick={onClick}
    >
      <span className="mr-3 align-middle text-xl font-medium">{title}</span>
      <span className="align-middle text-5xl font-black tracking-normal text-slate-950">{formatFlightRouteCity(route.departureAirport)}</span>
      <span className="mx-4 align-middle text-4xl font-light text-slate-300">→</span>
      <span className="align-middle text-5xl font-black tracking-normal text-slate-950">{formatFlightRouteCity(route.arrivalAirport)}</span>
      <span className="ml-4 align-middle text-xl font-medium text-slate-700">{formatDateLabel(route.departureDate)}</span>
    </button>
  )
}

function ResultsBody({
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

      <FlightFilterBar
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

      <div className="overflow-hidden bg-white">
        {resultsState.displayFlights.length > 0 ? (
          resultsState.displayFlights.map(displayFlight => (
            <FlightResultCard
              key={displayFlight.flight.flightId}
              displayFlight={displayFlight}
              isBusy={isBusy}
              selectedTravelerIds={selectedTravelerIds}
              getLateBookingNotice={getLateBookingNotice}
              onSubmit={event => void onSubmit(event, displayFlight, selectedTravelerIds)}
            />
          ))
        ) : (
          <p className="m-0 px-7 py-10 text-lg text-slate-500">{translate('flights.empty')}</p>
        )}
      </div>
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
                {item.lowestPrice ? `¥${formatPrice(item.lowestPrice)}` : '--'}
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

type FlightFilterBarProps = {
  airlineOptions: string[]
  departureAirportOptions: string[]
  arrivalAirportOptions: string[]
  cabinOptions: string[]
  selectedAirline: string
  selectedTimeRange: string
  selectedDepartureAirport: string
  selectedArrivalAirport: string
  selectedCabin: string
  sortMode: FlightSortMode
  onAirlineChange: (value: string) => void
  onTimeRangeChange: (value: string) => void
  onDepartureAirportChange: (value: string) => void
  onArrivalAirportChange: (value: string) => void
  onCabinChange: (value: string) => void
  onSortModeChange: (value: FlightSortMode) => void
  formatAirportName: (value: string) => string
  formatCabinLabel: (value: string) => string
}

function FlightFilterBar({
  airlineOptions,
  departureAirportOptions,
  arrivalAirportOptions,
  cabinOptions,
  selectedAirline,
  selectedTimeRange,
  selectedDepartureAirport,
  selectedArrivalAirport,
  selectedCabin,
  sortMode,
  onAirlineChange,
  onTimeRangeChange,
  onDepartureAirportChange,
  onArrivalAirportChange,
  onCabinChange,
  onSortModeChange,
  formatAirportName,
  formatCabinLabel,
}: FlightFilterBarProps) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-4 bg-white px-6 py-5">
      <div className="flex flex-wrap items-center gap-3">
        <FilterSelect label="航空公司" value={selectedAirline} onChange={onAirlineChange} options={airlineOptions} />
        <FilterSelect label="起抵时间" value={selectedTimeRange} onChange={onTimeRangeChange} options={departureTimeWindows} />
        <FilterSelect label="出发机场" value={selectedDepartureAirport} onChange={onDepartureAirportChange} options={departureAirportOptions} renderOption={formatAirportName} />
        <FilterSelect label="到达机场" value={selectedArrivalAirport} onChange={onArrivalAirportChange} options={arrivalAirportOptions} renderOption={formatAirportName} />
        <FilterSelect label="舱位" value={selectedCabin} onChange={onCabinChange} options={cabinOptions} renderOption={formatCabinLabel} />
      </div>

      <div className="flex flex-wrap items-center gap-5 text-base font-medium">
        <button
          type="button"
          className={sortMode === 'price' ? 'text-sky-600' : 'text-slate-800'}
          onClick={() => onSortModeChange('price')}
        >
          低价优先
        </button>
        <button
          type="button"
          className={sortMode === 'departureTime' ? 'text-sky-600' : 'text-slate-800'}
          onClick={() => onSortModeChange('departureTime')}
        >
          起飞时间早-晚
        </button>
      </div>
    </div>
  )
}

function FilterSelect({
  label,
  value,
  options,
  onChange,
  renderOption = option => option,
}: {
  label: string
  value: string
  options: string[]
  onChange: (value: string) => void
  renderOption?: (value: string) => string
}) {
  const [isOpen, setIsOpen] = useState(false)
  const selectedLabel = value === 'all' ? label : renderOption(value)
  const menuOptions = [{ value: 'all', label: '无要求' }, ...options.map(option => ({ value: option, label: renderOption(option) }))]

  function handleBlur(event: FocusEvent<HTMLDivElement>) {
    if (!event.currentTarget.contains(event.relatedTarget)) {
      setIsOpen(false)
    }
  }

  return (
    <div className="relative inline-flex min-w-40" onBlur={handleBlur}>
      <button
        type="button"
        className="flex h-14 w-full min-w-40 items-center justify-between border-2 border-slate-400 bg-white px-4 text-left text-lg text-slate-950 outline-none focus:border-sky-500"
        onClick={() => setIsOpen(open => !open)}
        aria-haspopup="listbox"
        aria-expanded={isOpen}
      >
        <span className="truncate">{selectedLabel}</span>
        <span className="ml-4 h-2.5 w-2.5 -translate-y-1 rotate-45 border-b-2 border-r-2 border-slate-950" />
      </button>
      {isOpen ? (
        <div className="absolute left-0 top-full z-30 w-max min-w-full border border-slate-300 bg-white shadow-lg" role="listbox">
          {menuOptions.map(option => (
            <button
              key={option.value}
              type="button"
              className={`block w-full px-4 py-2 text-left text-lg ${
                option.value === value ? 'bg-blue-600 text-white' : 'bg-white text-slate-950 hover:bg-slate-100'
              }`}
              onMouseDown={event => event.preventDefault()}
              onClick={() => {
                onChange(option.value)
                setIsOpen(false)
              }}
              role="option"
              aria-selected={option.value === value}
            >
              {option.label}
            </button>
          ))}
        </div>
      ) : null}
    </div>
  )
}

function FlightResultCard({
  displayFlight,
  isBusy,
  selectedTravelerIds,
  getLateBookingNotice,
  onSubmit,
}: {
  displayFlight: DisplayFlight
  isBusy: boolean
  selectedTravelerIds: string[]
  getLateBookingNotice: (flightResponse: FlightPlannerResponse) => string
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}) {
  const flight = displayFlight.flight

  return (
    <article className="grid gap-5 border-b border-slate-200 px-7 py-6 last:border-b-0 xl:grid-cols-[minmax(240px,1.1fr)_minmax(360px,1.2fr)_minmax(190px,0.8fr)_auto]">
      <div className="flex items-center gap-5">
        <div className="flex h-16 w-16 items-center justify-center border-2 border-slate-200 bg-slate-950 text-sm font-medium text-white">
          {displayFlight.airlineLogoPath ? <img src={displayFlight.airlineLogoPath} alt="" className="h-full w-full object-cover" /> : flight.airlineCode}
        </div>
        <div className="grid gap-1">
          <strong className="text-2xl font-bold text-slate-950">{displayFlight.airlineName}</strong>
          <div className="flex flex-wrap gap-3 text-base font-medium text-sky-600">
            <span>{flight.flightNumber}</span>
            <span>{flight.aircraftModel}</span>
            <span>{displayFlight.displayCabinLabel}</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-5">
        <TimeBlock time={formatFlightClock(flight.departureTime)} airport={displayFlight.departureAirportName} />
        <div className="h-px min-w-24 bg-slate-200" />
        <TimeBlock time={formatFlightClock(flight.arrivalTime)} airport={displayFlight.arrivalAirportName} />
      </div>

      <div className="grid content-center justify-items-end gap-1">
        <strong className={`text-4xl font-bold ${getPriceToneClass(displayFlight.priceTone)}`}>
          ¥{formatPrice(displayFlight.displayPrice)}
        </strong>
        <span className="text-base text-slate-500">{getPriceToneLabel(displayFlight.priceTone)}</span>
      </div>

      <form className="grid content-center justify-items-end gap-3" onSubmit={onSubmit}>
        <button
          type="submit"
          className="inline-flex min-h-14 min-w-28 items-center justify-center bg-pink-500 px-6 py-3 text-lg font-bold text-white transition hover:bg-pink-600 disabled:cursor-not-allowed disabled:bg-slate-300"
          disabled={isBusy || !displayFlight.isDisplayCabinBookable || selectedTravelerIds.length === 0}
        >
          订票
        </button>
        {flight.bookingWindowStatus === 'SurchargeRequired' ? (
          <p className="m-0 max-w-52 text-right text-sm text-amber-700">{getLateBookingNotice(flight)}</p>
        ) : null}
      </form>
    </article>
  )
}

function FlightTravelerSelectionPanel({
  travelers,
  selectedTravelerIds,
  onToggleTravelerSelection,
}: {
  travelers: FlightResultsSectionProps['travelers']
  selectedTravelerIds: string[]
  onToggleTravelerSelection: (travelerId: string) => void
}) {
  if (travelers.length === 0) {
    return null
  }

  return (
    <section className="grid gap-4 border border-slate-200 bg-white px-6 py-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h3 className="m-0 text-2xl font-bold text-slate-950">选择出行人</h3>
        <p className="m-0 text-sm font-medium text-slate-500">这里勾选的出行人会直接带到支付页</p>
      </div>
      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        {travelers.map(traveler => {
          const isSelected = selectedTravelerIds.includes(traveler.travelerId)
          return (
            <label
              key={traveler.travelerId}
              className={`flex cursor-pointer items-center gap-4 border px-4 py-4 text-base font-semibold transition ${
                isSelected ? 'border-sky-500 bg-sky-50 text-slate-950' : 'border-slate-200 bg-white text-slate-600 hover:border-slate-400'
              }`}
            >
              <input type="checkbox" checked={isSelected} onChange={() => onToggleTravelerSelection(traveler.travelerId)} />
              <span className="inline-flex h-11 w-11 items-center justify-center border border-slate-300 bg-slate-50 text-lg font-black text-slate-700">
                {traveler.fullName.slice(0, 1)}
              </span>
              <span className="truncate">{`${traveler.fullName} (${traveler.documentNumber.slice(-4)})`}</span>
            </label>
          )
        })}
      </div>
      {selectedTravelerIds.length === 0 ? <p className="m-0 text-sm font-medium text-rose-600">请至少选择一位出行人</p> : null}
    </section>
  )
}

function TimeBlock({ time, airport }: { time: string; airport: string }) {
  return (
    <div className="grid gap-1">
      <strong className="text-4xl font-bold leading-none text-slate-950">{time}</strong>
      <span className="text-base text-slate-600">{airport}</span>
    </div>
  )
}

function formatFlightClock(isoDateTime: string | null): string {
  if (!isoDateTime) {
    return '--:--'
  }

  return new Date(isoDateTime).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

function formatDateLabel(date: string): string {
  return new Date(`${date}T00:00:00`).toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
  })
}

function formatWeekday(date: string): string {
  return new Date(`${date}T00:00:00`).toLocaleDateString('zh-CN', {
    weekday: 'short',
  })
}

function formatPrice(value: string | number): string {
  const numeric = Number(value)
  if (!Number.isFinite(numeric)) {
    return '--'
  }
  return numeric % 1 === 0 ? numeric.toFixed(0) : numeric.toFixed(2)
}

function getPriceToneClass(kind: DisplayFlight['priceTone']) {
  if (kind === 'lowest') {
    return 'text-orange-500'
  }

  if (kind === 'discount') {
    return 'text-sky-600'
  }

  return 'text-slate-950'
}

function getPriceToneLabel(kind: DisplayFlight['priceTone']) {
  if (kind === 'lowest') {
    return '最低价'
  }

  if (kind === 'discount') {
    return '折扣价'
  }

  return '标准价'
}
