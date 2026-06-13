import { useEffect, useMemo, useState, type FormEvent } from 'react'

import type { FlightSearchState } from '@/app/stores/models/flights'
import { formatDateLabel, formatFlightRouteCity } from './FlightResultsUtils'
import { FlightResultsSummary } from './FlightResultsSummary'
import { useFlightResultsState } from '@/pages/FlightsPage/components/hooks/useFlightResultsState'
import type { DisplayFlight, FlightResultsRoute, FlightResultsSectionProps } from '../../objects'

// 往返结果区当前激活的航段标签。
type RoundTripLeg = 'outbound' | 'return'

// 不同航段在结果区里的主题样式，只影响外观不影响逻辑。
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

// FlightsPage 结果展示区，负责承接筛选、日期条和航班列表。
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
          <FlightResultsSummary
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
          <FlightResultsSummary
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
        <FlightResultsSummary
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

// 往返结果区的两个航段标签，负责在去程和返程之间切换。
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
        title="去程"
        route={outboundRoute}
        onClick={() => onChange('outbound')}
      />
      <LegTab
        isActive={activeLeg === 'return'}
        theme={legTheme.return}
        title="返程"
        route={returnRoute}
        onClick={() => onChange('return')}
      />
    </div>
  )
}

// 多程结果区的航段标签窗口，负责分页展示多段航程。
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
            title={`第 ${firstVisibleIndex + index + 1} 程`}
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

// 左右滚动按钮，只负责移动航段标签窗口。
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

// 单个航段标签，把航线和日期摘要拼成可点击按钮。
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
