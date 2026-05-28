import type { TrainHotRoute } from '@/pages/TrainsPage/components/controls/HotRoutes'
import { HotRoutes } from '@/pages/TrainsPage/components/controls/HotRoutes'
import { PassengerSelector } from '@/pages/TrainsPage/components/controls/PassengerSelector'
import { SeatClassSelector } from '@/pages/TrainsPage/components/controls/SeatClassSelector'
import { TrainTypeSelector } from '@/pages/TrainsPage/components/controls/TrainTypeSelector'
import { TripTypeSelector } from '@/pages/TrainsPage/components/controls/TripTypeSelector'
import type { TrainQuickDatePreset, TrainSeatPreference, TrainTripType, TrainTypePreference } from '@/app/stores/models/train-booking-model'

const quickDatePresets: TrainQuickDatePreset[] = ['today', 'tomorrow', 'weekend', 'nextWeek']

type TrainSearchCardProps = {
  isBusy: boolean
  tripType: TrainTripType
  searchDate: string
  returnDate: string
  searchFromStation: string
  searchToStation: string
  passengerCount: number
  seatPreference: TrainSeatPreference
  trainTypePreference: TrainTypePreference
  selectedQuickDatePreset: TrainQuickDatePreset | null
  hotRoutes: TrainHotRoute[]
  recentSearches: string[]
  popularStations: string[]
  earliestDepartureHint: string
  lowestPriceHint: string
  translate: (translationKey: string) => string
  onTripTypeChange: (value: TrainTripType) => void
  onSearchDateChange: (value: string) => void
  onReturnDateChange: (value: string) => void
  onSearchFromStationChange: (value: string) => void
  onSearchToStationChange: (value: string) => void
  onPassengerCountChange: (value: number) => void
  onSeatPreferenceChange: (value: TrainSeatPreference) => void
  onTrainTypePreferenceChange: (value: TrainTypePreference) => void
  onSelectQuickDatePreset: (preset: TrainQuickDatePreset) => void
  onSelectRoute: (route: TrainHotRoute) => void
  onSearch: () => void
}

export function TrainSearchCard({
  isBusy,
  tripType,
  searchDate,
  returnDate,
  searchFromStation,
  searchToStation,
  passengerCount,
  seatPreference,
  trainTypePreference,
  selectedQuickDatePreset,
  hotRoutes,
  recentSearches,
  popularStations,
  earliestDepartureHint,
  lowestPriceHint,
  translate,
  onTripTypeChange,
  onSearchDateChange,
  onReturnDateChange,
  onSearchFromStationChange,
  onSearchToStationChange,
  onPassengerCountChange,
  onSeatPreferenceChange,
  onTrainTypePreferenceChange,
  onSelectQuickDatePreset,
  onSelectRoute,
  onSearch,
}: TrainSearchCardProps) {
  return (
    <section className="grid gap-6 border border-sky-100 bg-gradient-to-br from-white via-sky-50 to-indigo-50 p-6 text-slate-950 shadow-lg shadow-sky-100/50">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="grid gap-2">
          <p className="text-sm font-bold text-sky-600">{translate('trains.title')}</p>
          <h2 className="m-0 text-4xl font-black leading-tight text-slate-950">{translate('trains.searchModuleTitle')}</h2>
          <p className="m-0 max-w-4xl text-base leading-7 text-slate-600">{translate('trains.description')}</p>
        </div>

        <div className="grid gap-3 md:grid-cols-2">
          <div className="border border-sky-100 bg-white p-4 text-sm leading-6 text-slate-600 shadow-sm shadow-sky-100/40">
            <span className="block text-slate-500">{translate('trains.earliestDeparture')}</span>
            <strong className="block text-lg font-black text-slate-950">{earliestDepartureHint}</strong>
          </div>
          <div className="border border-fuchsia-100 bg-white p-4 text-sm leading-6 text-slate-600 shadow-sm shadow-fuchsia-100/40">
            <span className="block text-slate-500">{translate('trains.lowestPrice')}</span>
            <strong className="block text-lg font-black text-slate-950">{lowestPriceHint}</strong>
          </div>
        </div>
      </div>

      <HotRoutes routes={hotRoutes} translate={translate} onSelectRoute={onSelectRoute} />
      <TripTypeSelector value={tripType} translate={translate} onChange={onTripTypeChange} />

      <div className={`grid gap-4 md:grid-cols-3 ${tripType === 'oneWay' ? 'items-end' : 'items-start'}`}>
        <label className="grid gap-2 text-sm font-medium text-slate-600">
          <span>{translate('trains.fromStation')}</span>
          <input
            list="train-station-suggestions"
            value={searchFromStation}
            onChange={event => onSearchFromStationChange(event.target.value)}
          />
        </label>
        <label className="grid gap-2 text-sm font-medium text-slate-600">
          <span>{translate('trains.toStation')}</span>
          <input
            list="train-station-suggestions"
            value={searchToStation}
            onChange={event => onSearchToStationChange(event.target.value)}
          />
        </label>
        <label className="grid gap-2 text-sm font-medium text-slate-600">
          <span>{translate('trains.date')}</span>
          <input type="date" value={searchDate} onChange={event => onSearchDateChange(event.target.value)} />
        </label>
        {tripType === 'roundTrip' ? (
          <label className="grid gap-2 text-sm font-medium text-slate-600 md:col-span-3">
            <span>{translate('trains.returnDate')}</span>
            <input type="date" value={returnDate} onChange={event => onReturnDateChange(event.target.value)} />
          </label>
        ) : null}
      </div>

      <div className="grid gap-2">
        <span className="text-sm font-medium text-slate-500">{translate('trains.quickDate')}</span>
        <div className="flex flex-wrap items-center gap-3">
          {quickDatePresets.map(preset => (
            <button
              key={preset}
              type="button"
              className={`inline-flex min-h-10 items-center justify-center border px-4 py-2 text-sm font-semibold shadow-none transition disabled:cursor-not-allowed disabled:opacity-55 ${
                selectedQuickDatePreset === preset
                  ? 'border-sky-600 bg-sky-600 text-white'
                  : 'border-slate-300 bg-white text-slate-950 hover:border-slate-950 hover:bg-slate-950 hover:text-white'
              }`}
              onClick={() => onSelectQuickDatePreset(preset)}
            >
              {translate(`trains.quickDate.${preset}`)}
            </button>
          ))}
        </div>
      </div>

      <div className="grid gap-4 xl:grid-cols-[1fr_1fr_1fr_auto] items-end">
        <PassengerSelector passengerCount={passengerCount} translate={translate} onChange={onPassengerCountChange} />
        <SeatClassSelector value={seatPreference} translate={translate} onChange={onSeatPreferenceChange} />
        <TrainTypeSelector value={trainTypePreference} translate={translate} onChange={onTrainTypePreferenceChange} />
        <button
          type="button"
          className="inline-flex min-h-12 items-center justify-center border border-sky-500 bg-sky-500 px-5 py-2 font-black text-white shadow-xl shadow-sky-200/70 transition hover:border-slate-950 hover:bg-slate-950 disabled:cursor-not-allowed disabled:opacity-55"
          onClick={onSearch}
          disabled={isBusy}
        >
          {translate('trains.search')}
        </button>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <div className="grid gap-2">
          <span className="text-sm font-medium text-slate-500">{translate('trains.recentSearches')}</span>
          <div className="flex flex-wrap items-center gap-3">
            {recentSearches.map(item => (
              <span key={item} className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">
                {item}
              </span>
            ))}
          </div>
        </div>
        <div className="grid gap-2">
          <span className="text-sm font-medium text-slate-500">{translate('trains.popularStations')}</span>
          <div className="flex flex-wrap items-center gap-3">
            {popularStations.map(item => (
              <span key={item} className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">
                {item}
              </span>
            ))}
          </div>
        </div>
      </div>

      <datalist id="train-station-suggestions">
        {[...recentSearches, ...popularStations].map(item => (
          <option key={item} value={item} />
        ))}
      </datalist>
    </section>
  )
}
