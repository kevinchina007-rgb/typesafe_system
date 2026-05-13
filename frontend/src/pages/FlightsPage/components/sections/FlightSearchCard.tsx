import type { HotRoute } from '@/app/stores/models/flights/flightConstants'
import type { TripType } from '@/app/stores/models/flights/flightTypes'
﻿import { CabinSelector } from '@/pages/FlightsPage/components/controls/CabinSelector'
import { HotRoutes } from '@/pages/FlightsPage/components/controls/HotRoutes'
import { PassengerSelector } from '@/pages/FlightsPage/components/controls/PassengerSelector'
import { TripTypeSelector } from '@/pages/FlightsPage/components/controls/TripTypeSelector'
import type { FlightSearchSegment, QuickDatePreset } from '@/app/stores/models/flights/flightTypes'

type FlightSearchCardProps = {
  tripType: TripType
  departureAirport: string
  arrivalAirport: string
  departureDate: string
  returnDate: string
  selectedQuickDatePreset: QuickDatePreset | null
  multiCitySegments: FlightSearchSegment[]
  adults: number
  childrenCount: number
  cabinPreference: string
  hotRoutes: HotRoute[]
  recentSearches: string[]
  popularCities: string[]
  priceInsight: string
  recommendationLabel: string
  translate: (translationKey: string) => string
  onTripTypeChange: (value: TripType) => void
  onDepartureAirportChange: (value: string) => void
  onArrivalAirportChange: (value: string) => void
  onDepartureDateChange: (value: string) => void
  onReturnDateChange: (value: string) => void
  onMultiCitySegmentChange: (
    segmentId: string,
    key: 'departureAirport' | 'arrivalAirport' | 'departureDate' | 'arrivalDate',
    value: string,
  ) => void
  onAddMultiCitySegment: () => void
  onRemoveMultiCitySegment: (segmentId: string) => void
  onAdultsChange: (value: number) => void
  onChildrenChange: (value: number) => void
  onCabinPreferenceChange: (value: string) => void
  onSelectRoute: (route: HotRoute) => void
  onQuickDateSelect: (preset: QuickDatePreset) => void
  onSubmit: () => void
}

const quickDatePresets: QuickDatePreset[] = ['today', 'tomorrow', 'weekend', 'nextWeek']

export function FlightSearchCard({
  tripType,
  departureAirport,
  arrivalAirport,
  departureDate,
  returnDate,
  selectedQuickDatePreset,
  multiCitySegments,
  adults,
  childrenCount,
  cabinPreference,
  hotRoutes,
  recentSearches,
  popularCities,
  priceInsight,
  recommendationLabel,
  translate,
  onTripTypeChange,
  onDepartureAirportChange,
  onArrivalAirportChange,
  onDepartureDateChange,
  onReturnDateChange,
  onMultiCitySegmentChange,
  onAddMultiCitySegment,
  onRemoveMultiCitySegment,
  onAdultsChange,
  onChildrenChange,
  onCabinPreferenceChange,
  onSelectRoute,
  onQuickDateSelect,
  onSubmit,
}: FlightSearchCardProps) {
  return (
    <section className="flight-search-card app-card">
      <div className="flight-search-card-head">
        <div>
          <p className="eyebrow-label">{translate('nav.flights')}</p>
          <h2 className="flight-search-card-title">{translate('flights.searchModuleTitle')}</h2>
        </div>
        <div className="flight-search-card-tip">
          <strong>{priceInsight}</strong>
          <span>{recommendationLabel}</span>
        </div>
      </div>

      <HotRoutes routes={hotRoutes} translate={translate} onSelectRoute={onSelectRoute} />

      <TripTypeSelector value={tripType} translate={translate} onChange={onTripTypeChange} />

      {tripType === 'multiCity' ? (
        <div className="flight-multi-city-panel">
          <div className="flight-multi-city-head">
            <span className="flight-quick-date-label">{translate('flights.multiCitySegments')}</span>
            <button type="button" className="flight-tag-button" onClick={onAddMultiCitySegment}>
              {translate('flights.addSegment')}
            </button>
          </div>
          <div className="flight-multi-city-list">
            {multiCitySegments.map((segment, index) => (
              <div key={segment.id} className="flight-search-grid flight-search-grid-primary flight-search-grid-multicity">
                <label className="flight-search-label">
                  <span>{`${translate('flights.segment')} ${index + 1} · ${translate('flights.departureAirport')}`}</span>
                  <input
                    list="flight-city-suggestions"
                    value={segment.departureAirport}
                    onChange={event => onMultiCitySegmentChange(segment.id, 'departureAirport', event.target.value)}
                    placeholder={translate('flights.departurePlaceholder')}
                  />
                </label>
                <label className="flight-search-label">
                  <span>{translate('flights.arrivalAirport')}</span>
                  <input
                    list="flight-city-suggestions"
                    value={segment.arrivalAirport}
                    onChange={event => onMultiCitySegmentChange(segment.id, 'arrivalAirport', event.target.value)}
                    placeholder={translate('flights.arrivalPlaceholder')}
                  />
                </label>
                <label className="flight-search-label">
                  <span>{translate('flights.date')}</span>
                  <input
                    type="date"
                    value={segment.departureDate}
                    onChange={event => onMultiCitySegmentChange(segment.id, 'departureDate', event.target.value)}
                  />
                </label>
                <label className="flight-search-label">
                  <span>{translate('flights.arrivalDate')}</span>
                  <input
                    type="date"
                    value={segment.arrivalDate}
                    onChange={event => onMultiCitySegmentChange(segment.id, 'arrivalDate', event.target.value)}
                  />
                </label>
                <div className="flight-multi-city-actions">
                  <button
                    type="button"
                    className="flight-tag-button"
                    onClick={() => onRemoveMultiCitySegment(segment.id)}
                    disabled={multiCitySegments.length <= 2}
                  >
                    {translate('flights.removeSegment')}
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : (
        <div className={`flight-search-grid flight-search-grid-primary ${tripType === 'oneWay' ? 'is-one-way' : ''}`}>
          <label className="flight-search-label">
            <span>{translate('flights.departureAirport')}</span>
            <input
              list="flight-city-suggestions"
              value={departureAirport}
              onChange={event => onDepartureAirportChange(event.target.value)}
              placeholder={translate('flights.departurePlaceholder')}
            />
          </label>
          <label className="flight-search-label">
            <span>{translate('flights.arrivalAirport')}</span>
            <input
              list="flight-city-suggestions"
              value={arrivalAirport}
              onChange={event => onArrivalAirportChange(event.target.value)}
              placeholder={translate('flights.arrivalPlaceholder')}
            />
          </label>
          <label className="flight-search-label">
            <span>{translate('flights.date')}</span>
            <input type="date" value={departureDate} onChange={event => onDepartureDateChange(event.target.value)} />
          </label>
          {tripType === 'roundTrip' ? (
            <label className="flight-search-label">
              <span>{translate('flights.returnDate')}</span>
              <input type="date" value={returnDate} onChange={event => onReturnDateChange(event.target.value)} />
            </label>
          ) : null}
        </div>
      )}

      <div className="flight-quick-date-row">
        <span className="flight-quick-date-label">{translate('flights.quickDate')}</span>
        <div className="flight-quick-date-list">
          {quickDatePresets.map(preset => (
            <button
              key={preset}
              type="button"
              className={`flight-chip-button ${selectedQuickDatePreset === preset ? 'is-active' : ''}`}
              onClick={() => onQuickDateSelect(preset)}
            >
              <span className="flight-chip-indicator" aria-hidden="true" />
              {translate(`flights.quickDate.${preset}`)}
            </button>
          ))}
        </div>
      </div>

      <div className="flight-search-grid flight-search-grid-secondary">
        <PassengerSelector
          adults={adults}
          childrenCount={childrenCount}
          translate={translate}
          onAdultsChange={onAdultsChange}
          onChildrenChange={onChildrenChange}
        />
        <CabinSelector value={cabinPreference} translate={translate} onChange={onCabinPreferenceChange} />
        <button type="button" className="flight-search-submit-button" onClick={onSubmit}>
          {translate('flights.search')}
        </button>
      </div>

      <div className="flight-search-assist">
        <div className="flight-search-assist-block">
          <span className="flight-search-assist-label">{translate('flights.recentSearches')}</span>
          <div className="flight-search-assist-tags">
            {recentSearches.map(item => (
              <span key={item} className="flight-tag-muted">
                {item}
              </span>
            ))}
          </div>
        </div>
        <div className="flight-search-assist-block">
          <span className="flight-search-assist-label">{translate('flights.popularCities')}</span>
          <div className="flight-search-assist-tags">
            {popularCities.map(item => (
              <span key={item} className="flight-tag-muted">
                {item}
              </span>
            ))}
          </div>
        </div>
      </div>

      <datalist id="flight-city-suggestions">
        {[...recentSearches, ...popularCities].map(item => (
          <option key={item} value={item} />
        ))}
      </datalist>
    </section>
  )
}






