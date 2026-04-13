import { CabinSelector } from './CabinSelector'
import { HotRoutes, type HotRoute } from './HotRoutes'
import { PassengerSelector } from './PassengerSelector'
import { TripTypeSelector, type TripType } from './TripTypeSelector'

type FlightSearchCardProps = {
  tripType: TripType
  departureAirport: string
  arrivalAirport: string
  departureDate: string
  returnDate: string
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
  onAdultsChange: (value: number) => void
  onChildrenChange: (value: number) => void
  onCabinPreferenceChange: (value: string) => void
  onSelectRoute: (route: HotRoute) => void
  onQuickDateSelect: (preset: 'today' | 'tomorrow' | 'weekend' | 'nextWeek') => void
  onSubmit: () => void
}

const quickDatePresets: Array<'today' | 'tomorrow' | 'weekend' | 'nextWeek'> = ['today', 'tomorrow', 'weekend', 'nextWeek']

export function FlightSearchCard({
  tripType,
  departureAirport,
  arrivalAirport,
  departureDate,
  returnDate,
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

      <div className="flight-search-grid flight-search-grid-primary">
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
        <label className="flight-search-label">
          <span>{translate('flights.returnDate')}</span>
          <input
            type="date"
            value={returnDate}
            disabled={tripType !== 'roundTrip'}
            onChange={event => onReturnDateChange(event.target.value)}
          />
        </label>
      </div>

      <div className="flight-quick-date-row">
        <span className="flight-quick-date-label">{translate('flights.quickDate')}</span>
        <div className="flight-quick-date-list">
          {quickDatePresets.map(preset => (
            <button key={preset} type="button" className="flight-chip-button" onClick={() => onQuickDateSelect(preset)}>
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
