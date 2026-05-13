import type { TrainHotRoute } from '@/pages/TrainsPage/components/controls/HotRoutes'
﻿import { HotRoutes } from '@/pages/TrainsPage/components/controls/HotRoutes'
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
    <section className="resource-search-card app-card">
      <div className="resource-search-card-head">
        <div>
          <p className="eyebrow-label">{translate('nav.trains')}</p>
          <h2 className="resource-search-card-title">{translate('trains.searchModuleTitle')}</h2>
        </div>
        <div className="resource-search-card-tip-grid">
          <div className="resource-search-card-tip">
            <span>{translate('trains.earliestDeparture')}</span>
            <strong>{earliestDepartureHint}</strong>
          </div>
          <div className="resource-search-card-tip">
            <span>{translate('trains.lowestPrice')}</span>
            <strong>{lowestPriceHint}</strong>
          </div>
        </div>
      </div>

      <HotRoutes routes={hotRoutes} translate={translate} onSelectRoute={onSelectRoute} />
      <TripTypeSelector value={tripType} translate={translate} onChange={onTripTypeChange} />

      <div className={`resource-search-grid resource-search-grid-primary ${tripType === 'oneWay' ? 'is-compact' : ''}`}>
        <label className="resource-search-label">
          <span>{translate('trains.fromStation')}</span>
          <input list="train-station-suggestions" value={searchFromStation} onChange={event => onSearchFromStationChange(event.target.value)} />
        </label>
        <label className="resource-search-label">
          <span>{translate('trains.toStation')}</span>
          <input list="train-station-suggestions" value={searchToStation} onChange={event => onSearchToStationChange(event.target.value)} />
        </label>
        <label className="resource-search-label">
          <span>{translate('trains.date')}</span>
          <input type="date" value={searchDate} onChange={event => onSearchDateChange(event.target.value)} />
        </label>
        {tripType === 'roundTrip' ? (
          <label className="resource-search-label">
            <span>{translate('trains.returnDate')}</span>
            <input type="date" value={returnDate} onChange={event => onReturnDateChange(event.target.value)} />
          </label>
        ) : null}
      </div>

      <div className="resource-quick-date-row">
        <span className="resource-quick-date-label">{translate('trains.quickDate')}</span>
        <div className="resource-quick-date-list">
          {quickDatePresets.map(preset => (
            <button
              key={preset}
              type="button"
              className={`resource-chip-button ${selectedQuickDatePreset === preset ? 'is-active' : ''}`}
              onClick={() => onSelectQuickDatePreset(preset)}
            >
              {translate(`trains.quickDate.${preset}`)}
            </button>
          ))}
        </div>
      </div>

      <div className="resource-search-grid resource-search-grid-secondary resource-search-grid-trains">
        <PassengerSelector passengerCount={passengerCount} translate={translate} onChange={onPassengerCountChange} />
        <SeatClassSelector value={seatPreference} translate={translate} onChange={onSeatPreferenceChange} />
        <TrainTypeSelector value={trainTypePreference} translate={translate} onChange={onTrainTypePreferenceChange} />
        <button type="button" className="resource-search-submit-button" onClick={onSearch} disabled={isBusy}>
          {translate('trains.search')}
        </button>
      </div>

      <div className="resource-search-assist">
        <div className="resource-search-assist-block">
          <span className="resource-search-assist-label">{translate('trains.recentSearches')}</span>
          <div className="resource-search-assist-tags">
            {recentSearches.map(item => (
              <span key={item} className="resource-tag-muted">{item}</span>
            ))}
          </div>
        </div>
        <div className="resource-search-assist-block">
          <span className="resource-search-assist-label">{translate('trains.popularStations')}</span>
          <div className="resource-search-assist-tags">
            {popularStations.map(item => (
              <span key={item} className="resource-tag-muted">{item}</span>
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
