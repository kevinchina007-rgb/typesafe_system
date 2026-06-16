import type { ReactNode } from 'react'
import { ArrowLeftRight } from 'lucide-react'

import { formatFlightAirportLabel, formatFlightRouteCity } from '@/app/stores/models/flights/flightConstants'
import { getFlightDetailsPlannerAirlineDisplayNameByCode, getFlightDetailsPlannerAirlineLogoPathByCode } from '@/app/stores/models/flights/flightAirlineCatalog'
import type { ManagerFlightPlannerResponse } from '@/lib/mvp-types/manager'
import { localizeCabinClass, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import type { ManagerPanelProps } from '@/pages/ManagerPage/components/managers/manager-panel-shared'
import { formatFlightClock, type AirlineProfileDraft } from './manager-panel-workspace-profile'
import {
  CitySelect,
  FilterSelect,
  type ManagerFlightSearchDraft,
  TimeBlock,
  timeWindows,
} from './manager-panel-workspace-flight'

function ManagerFlightSearchCard({
  draft,
  onDraftChange,
  onSubmit,
  onSwapRoute,
}: {
  draft: ManagerFlightSearchDraft
  onDraftChange: (value: ManagerFlightSearchDraft) => void
  onSubmit: () => void
  onSwapRoute: () => void
}) {
  return (
    <section className="relative z-20 grid gap-6 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/70">
      <div className="grid gap-4 xl:grid-cols-[1.35fr_auto_1.35fr_1.05fr_1fr]">
        <ManagerSearchField label="出发地">
          <CitySelect
            value={draft.departureCity}
            placeholder="出发地"
            onChange={event => onDraftChange({ ...draft, departureCity: event.target.value })}
          />
        </ManagerSearchField>

        <div className="flex items-end justify-center">
          <button
            type="button"
            aria-label="交换出发地和目的地"
            className="mb-1 inline-flex h-14 w-14 items-center justify-center border-2 border-slate-300 bg-white text-slate-600 transition hover:border-slate-950 hover:text-slate-950"
            onClick={onSwapRoute}
          >
            <ArrowLeftRight className="h-6 w-6" />
          </button>
        </div>

        <ManagerSearchField label="目的地">
          <CitySelect
            value={draft.arrivalCity}
            placeholder="目的地"
            onChange={event => onDraftChange({ ...draft, arrivalCity: event.target.value })}
          />
        </ManagerSearchField>

        <ManagerSearchField label="出发日期">
          <input
            type="date"
            value={draft.departureDate}
            onChange={event => onDraftChange({ ...draft, departureDate: event.target.value })}
            className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950"
          />
        </ManagerSearchField>

        <ManagerSearchField label="出发时段">
          <select
            value={draft.timeRange}
            onChange={event => onDraftChange({ ...draft, timeRange: event.target.value })}
            className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950"
          >
            <option value="all">全天</option>
            {timeWindows.map(option => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </ManagerSearchField>
      </div>

      <div className="flex justify-center">
        <button
          type="button"
          className="inline-flex min-h-14 min-w-64 items-center justify-center bg-gradient-to-r from-amber-400 to-orange-500 px-10 py-3 text-xl font-bold text-white shadow-xl shadow-orange-200/70 transition hover:from-amber-500 hover:to-orange-600"
          onClick={onSubmit}
        >
          搜索航班
        </button>
      </div>
    </section>
  )
}

// 查询字段包装器，统一展示字段标题和输入控件。
function ManagerSearchField({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="grid gap-2">
      <span className="text-base font-medium text-slate-500">{label}</span>
      {children}
    </label>
  )
}

// 城市下拉选择器，负责展示所有可选出发/到达城市。
export function FlightManagementSection({
  currentLanguage,
  flights,
  totalCount,
  searchDraft,
  hasSubmittedSearch,
  sortDirection,
  selectedDepartureAirport,
  selectedArrivalAirport,
  departureAirportOptions,
  arrivalAirportOptions,
  profile,
  translate,
  onSearchDraftChange,
  onSearchSubmit,
  onSwapSearchRoute,
  onOpenFlight,
  onToggleFlightStatus,
  onDepartureAirportChange,
  onArrivalAirportChange,
  onSortDirectionChange,
}: {
  currentLanguage: ManagerPanelProps['currentLanguage']
  flights: ManagerFlightPlannerResponse[]
  totalCount: number
  searchDraft: ManagerFlightSearchDraft
  hasSubmittedSearch: boolean
  sortDirection: 'asc' | 'desc'
  selectedDepartureAirport: string
  selectedArrivalAirport: string
  departureAirportOptions: string[]
  arrivalAirportOptions: string[]
  profile: AirlineProfileDraft
  translate: (translationKey: string) => string
  onSearchDraftChange: (value: ManagerFlightSearchDraft) => void
  onSearchSubmit: () => void
  onSwapSearchRoute: () => void
  onOpenFlight: (flight: ManagerFlightPlannerResponse) => void | Promise<void>
  onToggleFlightStatus: (flightId: string) => void | Promise<void>
  onDepartureAirportChange: (value: string) => void
  onArrivalAirportChange: (value: string) => void
  onSortDirectionChange: (value: 'asc' | 'desc') => void
}) {
  return (
    <section className="grid gap-6 bg-slate-100 px-6 pb-8 pt-8">
      <ManagerFlightSearchCard
        draft={searchDraft}
        onDraftChange={onSearchDraftChange}
        onSubmit={onSearchSubmit}
        onSwapRoute={onSwapSearchRoute}
      />

      <div className="flex flex-wrap items-end justify-between gap-4">
        <h3 className="m-0 text-3xl font-bold text-slate-950">
          <span className="mr-3 text-xl font-medium">航班管理:</span>
          {profile.companyName || '航空公司'}
          <span className="ml-4 text-lg font-medium text-slate-600">
            {hasSubmittedSearch ? `${flights.length} / ${totalCount} 条航班` : `${totalCount} 条航班`}
          </span>
        </h3>
      </div>

      <div className="grid items-center gap-4 bg-white px-6 py-5 lg:grid-cols-[minmax(0,1fr)_auto]">
        <div className="grid max-w-3xl grid-cols-2 gap-3">
          <FilterSelect label="出发机场" value={selectedDepartureAirport} onChange={onDepartureAirportChange} options={departureAirportOptions} renderOption={formatFlightAirportLabel} />
          <FilterSelect label="到达机场" value={selectedArrivalAirport} onChange={onArrivalAirportChange} options={arrivalAirportOptions} renderOption={formatFlightAirportLabel} />
        </div>
        <button
          type="button"
          className="text-base font-medium text-sky-600 transition hover:text-slate-950"
          onClick={() => onSortDirectionChange(sortDirection === 'asc' ? 'desc' : 'asc')}
        >
          {sortDirection === 'asc' ? '起飞时间早-晚' : '起飞时间晚-早'}
        </button>
      </div>

      <div className="overflow-hidden bg-white">
        {flights.length > 0 ? (
          flights.map(flight => (
            <ManagerFlightCard
              key={flight.flightId}
              flight={flight}
              profile={profile}
              currentLanguage={currentLanguage}
              onOpen={() => void onOpenFlight(flight)}
              onToggleStatus={() => void onToggleFlightStatus(flight.flightId)}
            />
          ))
        ) : (
          <p className="m-0 px-7 py-10 text-lg text-slate-500">{translate('flights.empty')}</p>
        )}
      </div>
    </section>
  )
}

// 单条航班卡片，负责展示航班信息和启停按钮。
function ManagerFlightCard({
  flight,
  profile,
  currentLanguage,
  onOpen,
  onToggleStatus,
}: {
  flight: ManagerFlightPlannerResponse
  profile: AirlineProfileDraft
  currentLanguage: ManagerPanelProps['currentLanguage']
  onOpen: () => void
  onToggleStatus: () => void
}) {
  const airlineName = profile.companyName || getFlightDetailsPlannerAirlineDisplayNameByCode(flight.airlineCode, flight.airlineName)
  const logoPath = profile.logoPath || getFlightDetailsPlannerAirlineLogoPathByCode(flight.airlineCode, flight.airlineLogoPath)
  const isOpenForBooking = flight.status === 'OpenForBooking'
  const isClosedForBooking = flight.status === 'ClosedForBooking'

  return (
    <article
      className="grid cursor-pointer gap-5 border-b border-slate-200 px-7 py-6 transition hover:bg-slate-50 last:border-b-0 xl:grid-cols-[minmax(260px,1.1fr)_minmax(360px,1.4fr)_minmax(180px,0.7fr)]"
      role="button"
      tabIndex={0}
      onClick={onOpen}
      onKeyDown={event => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault()
          onOpen()
        }
      }}
    >
      <div className="flex items-center gap-5">
        <div className="flex h-16 w-16 items-center justify-center border-2 border-slate-200 bg-slate-950 text-sm font-medium text-white">
          {logoPath ? <img src={logoPath} alt="" className="h-full w-full object-cover" /> : flight.airlineCode}
        </div>
        <div className="grid gap-1">
          <strong className="text-2xl font-bold text-slate-950">{airlineName}</strong>
          <div className="flex flex-wrap gap-3 text-base font-medium text-sky-600">
            <span>{flight.flightNumber}</span>
            <span>{flight.aircraftModel}</span>
          </div>
          <div className="flex flex-wrap gap-2 text-sm text-slate-500">
            {(flight.cabinInventories ?? []).map(cabin => (
              <span key={cabin.inventoryId}>{localizeCabinClass(cabin.cabinClass, currentLanguage)}</span>
            ))}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-5">
        <TimeBlock time={formatFlightClock(flight.departureTime)} airport={formatFlightAirportLabel(flight.departureAirport)} />
        <div className="h-px min-w-24 bg-slate-200" />
        <TimeBlock time={formatFlightClock(flight.arrivalTime)} airport={formatFlightAirportLabel(flight.arrivalAirport)} />
      </div>

      <div className="grid content-center justify-items-end gap-2 text-right">
        <button
          type="button"
          className={[
            'inline-flex min-h-11 items-center justify-center border px-5 py-2 text-base font-black transition',
            isOpenForBooking
              ? 'border-emerald-200 bg-emerald-50 text-emerald-700 hover:border-emerald-700'
              : isClosedForBooking
                ? 'border-rose-200 bg-rose-50 text-rose-700 hover:border-rose-700'
                : 'border-slate-200 bg-slate-50 text-slate-700 hover:border-slate-700',
          ].join(' ')}
          onClick={event => {
            event.stopPropagation()
            onToggleStatus()
          }}
        >
          {isOpenForBooking ? '可预订' : isClosedForBooking ? '不可预订' : mapBackendStatusToProductLabel(flight.status, currentLanguage)}
        </button>
        <span className="text-sm text-slate-500">{`${formatFlightRouteCity(flight.departureAirport)} -> ${formatFlightRouteCity(flight.arrivalAirport)}`}</span>
      </div>
    </article>
  )
}

// 航班订单区块，负责按舱位分组展示当前航班的订单。
